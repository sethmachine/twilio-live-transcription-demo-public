package io.sethmachine.twiliolivetranscriptiondemo.core.concurrent.speech.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.speech.google.TranscriptOutputMessage;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.MediaMessage;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.StreamMessage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingSpeechToTextRunnable
  implements Runnable, MessageHandler.Whole<StreamMessage> {

  private static final Logger LOG = LoggerFactory.getLogger(
    StreamingSpeechToTextRunnable.class
  );

  private static final int STREAMING_LIMIT = 290000; // ~5 minutes

  public static final String RED = "\033[0;31m";
  public static final String GREEN = "\033[0;32m";
  public static final String YELLOW = "\033[0;33m";

  // Creating shared object
  private static volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue();
  private static int BYTES_PER_BUFFER = 6400; // buffer size in bytes

  private static int restartCounter = 0;
  private static ArrayList<ByteString> audioInput = new ArrayList<ByteString>();
  private static ArrayList<ByteString> lastAudioInput = new ArrayList<ByteString>();
  private static int resultEndTimeInMS = 0;
  private static int isFinalEndTime = 0;
  private static int finalRequestEndTime = 0;
  private static boolean newStream = true;
  private static double bridgingOffset = 0;
  private static boolean lastTranscriptWasFinal = false;
  private static StreamController referenceToStreamController;
  private static ByteString tempByteString;

  private Session websocketSession;

  private ObjectMapper objectMapper;

  private AtomicBoolean stopped = new AtomicBoolean(false);
  private Thread worker;

  @Inject
  public StreamingSpeechToTextRunnable(
    @Assisted Session websocketSession,
    ObjectMapper objectMapper
  ) {
    this.websocketSession = websocketSession;
    this.objectMapper = objectMapper;
  }

  public void stop() {
    LOG.info("Received request to stop this thread");
    this.stopped.set(true);
  }

  @Override
  public void run() {
    ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
    try (SpeechClient client = SpeechClient.create()) {
      ClientStream<StreamingRecognizeRequest> clientStream;
      responseObserver =
        new ResponseObserver<StreamingRecognizeResponse>() {
          ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

          public void onStart(StreamController controller) {
            referenceToStreamController = controller;
          }

          public void onResponse(StreamingRecognizeResponse response) {
            responses.add(response);
            StreamingRecognitionResult result = response.getResultsList().get(0);
            Duration resultEndTime = result.getResultEndTime();
            resultEndTimeInMS =
              (int) (
                (resultEndTime.getSeconds() * 1000) + (resultEndTime.getNanos() / 1000000)
              );
            double correctedTime =
              resultEndTimeInMS - bridgingOffset + (STREAMING_LIMIT * restartCounter);

            SpeechRecognitionAlternative alternative = result
              .getAlternativesList()
              .get(0);
            if (result.getIsFinal()) {
              isFinalEndTime = resultEndTimeInMS;
              lastTranscriptWasFinal = true;
              // in actual use we would publish to a specific channel tied to the call ID
              websocketSession
                .getOpenSessions()
                .forEach(session -> {
                  try {
                    session
                      .getAsyncRemote()
                      .sendObject(
                        objectMapper.writeValueAsString(
                          createTranscriptOutputMessage(result.getIsFinal(), alternative)
                        )
                      );
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                });
            } else {
              lastTranscriptWasFinal = false;
              LOG.info(
                "TRANSCRIPTION RESULT: transcript: {}, confidence {}",
                alternative.getTranscript(),
                alternative.getConfidence()
              );

              // in actual use we would publish to a specific channel tied to the call ID
              websocketSession
                .getOpenSessions()
                .forEach(session -> {
                  try {
                    session
                      .getAsyncRemote()
                      .sendText(
                        objectMapper.writeValueAsString(
                          createTranscriptOutputMessage(result.getIsFinal(), alternative)
                        )
                      );
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                });
            }
          }

          public void onComplete() {}

          public void onError(Throwable t) {}
        };
      clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

      RecognitionConfig recognitionConfig = RecognitionConfig
        .newBuilder()
        .setEncoding(AudioEncoding.MULAW)
        .setLanguageCode("en-US")
        .setSampleRateHertz(8000)
        .setModel("phone_call")
        .build();

      StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig
        .newBuilder()
        .setConfig(recognitionConfig)
        .setInterimResults(true)
        .build();

      StreamingRecognizeRequest request = StreamingRecognizeRequest
        .newBuilder()
        .setStreamingConfig(streamingRecognitionConfig)
        .build(); // The first request in a streaming call has to be a config

      clientStream.send(request);

      try {
        long startTime = System.currentTimeMillis();

        while (!stopped.get()) {
          long estimatedTime = System.currentTimeMillis() - startTime;

          if (estimatedTime >= STREAMING_LIMIT) {
            clientStream.closeSend();
            referenceToStreamController.cancel(); // remove Observer

            if (resultEndTimeInMS > 0) {
              finalRequestEndTime = isFinalEndTime;
            }
            resultEndTimeInMS = 0;

            lastAudioInput = null;
            lastAudioInput = audioInput;
            audioInput = new ArrayList<ByteString>();

            restartCounter++;

            if (!lastTranscriptWasFinal) {
              System.out.print('\n');
            }

            newStream = true;

            clientStream =
              client.streamingRecognizeCallable().splitCall(responseObserver);

            request =
              StreamingRecognizeRequest
                .newBuilder()
                .setStreamingConfig(streamingRecognitionConfig)
                .build();

            System.out.println(YELLOW);
            System.out.printf(
              "%d: RESTARTING REQUEST\n",
              restartCounter * STREAMING_LIMIT
            );

            startTime = System.currentTimeMillis();
          } else {
            if ((newStream) && (lastAudioInput.size() > 0)) {
              // if this is the first audio from a new request
              // calculate amount of unfinalized audio from last request
              // resend the audio to the speech client before incoming audio
              double chunkTime = STREAMING_LIMIT / lastAudioInput.size();
              // ms length of each chunk in previous request audio arrayList
              if (chunkTime != 0) {
                if (bridgingOffset < 0) {
                  // bridging Offset accounts for time of resent audio
                  // calculated from last request
                  bridgingOffset = 0;
                }
                if (bridgingOffset > finalRequestEndTime) {
                  bridgingOffset = finalRequestEndTime;
                }
                int chunksFromMs = (int) Math.floor(
                  (finalRequestEndTime - bridgingOffset) / chunkTime
                );
                // chunks from MS is number of chunks to resend
                bridgingOffset =
                  (int) Math.floor((lastAudioInput.size() - chunksFromMs) * chunkTime);
                // set bridging offset for next request
                for (int i = chunksFromMs; i < lastAudioInput.size(); i++) {
                  request =
                    StreamingRecognizeRequest
                      .newBuilder()
                      .setAudioContent(lastAudioInput.get(i))
                      .build();
                  clientStream.send(request);
                }
              }
              newStream = false;
            }

            tempByteString = ByteString.copyFrom(sharedQueue.take());

            request =
              StreamingRecognizeRequest
                .newBuilder()
                .setAudioContent(tempByteString)
                .build();

            audioInput.add(tempByteString);
          }

          clientStream.send(request);
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    LOG.info("Runnable has stopped!");
  }

  @Override
  public void onMessage(StreamMessage streamMessage) {
    MediaMessage mediaMessage = (MediaMessage) streamMessage;
    byte[] audioBytes = Base64
      .getDecoder()
      .decode(mediaMessage.getMediaMessagePayload().getPayload());
    try {
      sharedQueue.put(audioBytes);
    } catch (InterruptedException e) {
      LOG.error("Failed to add media message bytes to shared queue", e);
      throw new RuntimeException(e);
    }
  }

  public static String convertMillisToDate(double milliSeconds) {
    long millis = (long) milliSeconds;
    DecimalFormat format = new DecimalFormat();
    format.setMinimumIntegerDigits(2);
    return String.format(
      "%s:%s /",
      format.format(TimeUnit.MILLISECONDS.toMinutes(millis)),
      format.format(
        TimeUnit.MILLISECONDS.toSeconds(millis) -
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
      )
    );
  }

  private static TranscriptOutputMessage createTranscriptOutputMessage(
    boolean isFinal,
    SpeechRecognitionAlternative alternative
  ) {
    return TranscriptOutputMessage
      .builder()
      .setText(alternative.getTranscript().strip())
      .setConfidence(alternative.getConfidence())
      .setIsFinal(isFinal)
      .build();
  }
}
