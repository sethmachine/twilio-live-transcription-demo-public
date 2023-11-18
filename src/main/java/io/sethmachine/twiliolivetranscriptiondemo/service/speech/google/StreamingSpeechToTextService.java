package io.sethmachine.twiliolivetranscriptiondemo.service.speech.google;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.sethmachine.twiliolivetranscriptiondemo.core.concurrent.speech.google.StreamingSpeechToTextRunnable;
import io.sethmachine.twiliolivetranscriptiondemo.core.concurrent.speech.google.StreamingSpeechToTextRunnableFactory;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.ConnectedMessage;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.MediaMessage;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.StartMessage;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.StreamMessage;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingSpeechToTextService {

  private static final Logger LOG = LoggerFactory.getLogger(
    StreamingSpeechToTextService.class
  );

  private final ThreadPoolExecutor speechToTextThreadPoolExecutor;
  private final StreamingSpeechToTextRunnableFactory streamingSpeechToTextRunnableFactory;

  @Inject
  public StreamingSpeechToTextService(
    @Named(
      "StreamingCloudSpeechToTextThreadPoolExecutor"
    ) ThreadPoolExecutor threadPoolExecutor,
    StreamingSpeechToTextRunnableFactory streamingSpeechToTextRunnableFactory
  ) {
    this.speechToTextThreadPoolExecutor = threadPoolExecutor;
    this.streamingSpeechToTextRunnableFactory = streamingSpeechToTextRunnableFactory;
  }

  public void handleStreamMessage(Session session, StreamMessage streamMessage) {
    switch (streamMessage.getMessageEventType()) {
      case CONNECTED:
        handleConnectedMessage(session, (ConnectedMessage) streamMessage);
        break;
      case START:
        handleStartMessage(session, (StartMessage) streamMessage);
        break;
      case MEDIA:
        handleMediaMessage(session, (MediaMessage) streamMessage);
        break;
      case STOP:
        handleStreamClose(session);
        break;
      default:
        LOG.error(
          "[sessionId: {}] Unhandled message event type for StreamMessage: {}",
          session.getId(),
          streamMessage
        );
    }
  }

  public void handleStreamClose(Session session) {
    getRunnableFromSession(session)
      .ifPresentOrElse(
        StreamingSpeechToTextRunnable::stop,
        () -> LOG.info("Attempted to stop session but no runnable found: {}", session)
      );
  }

  private void handleConnectedMessage(
    Session session,
    ConnectedMessage connectedMessage
  ) {
    LOG.info(
      "[sessionId: {}] Received connected message: {}",
      session.getId(),
      connectedMessage
    );
  }

  private void handleStartMessage(Session session, StartMessage startMessage) {
    LOG.info("[sessionId: {}] Received start message: {}", session.getId(), startMessage);
    StreamingSpeechToTextRunnable streamingSpeechToTextRunnable = streamingSpeechToTextRunnableFactory.create(
      session
    );
    session.addMessageHandler(streamingSpeechToTextRunnable);
    speechToTextThreadPoolExecutor.execute(streamingSpeechToTextRunnable);
  }

  private void handleMediaMessage(Session session, MediaMessage mediaMessage) {
    StreamingSpeechToTextRunnable streamingSpeechToTextRunnable = getRunnableFromSession(
      session
    )
      .orElseThrow();
    streamingSpeechToTextRunnable.onMessage(mediaMessage);
  }

  private Optional<StreamingSpeechToTextRunnable> getRunnableFromSession(
    Session session
  ) {
    try {
      return Optional.of(
        (StreamingSpeechToTextRunnable) Iterables.getOnlyElement(
          session.getMessageHandlers()
        )
      );
    } catch (Exception e) {
      LOG.error("Failed to get runnable from session: {}", session, e);
      return Optional.empty();
    }
  }
}
