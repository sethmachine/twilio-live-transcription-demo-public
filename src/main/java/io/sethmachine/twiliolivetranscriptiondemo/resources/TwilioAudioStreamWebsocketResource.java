package io.sethmachine.twiliolivetranscriptiondemo.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.StreamMessage;
import io.sethmachine.twiliolivetranscriptiondemo.guice.GuiceWebsocketConfigurator;
import io.sethmachine.twiliolivetranscriptiondemo.service.speech.google.StreamingSpeechToTextService;
import io.sethmachine.twiliolivetranscriptiondemo.service.twilio.stream.StreamMessageDecoder;
import java.io.IOException;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint(
  value = "/twilio/websocket/audio-stream",
  configurator = GuiceWebsocketConfigurator.class,
  decoders = { StreamMessageDecoder.class }
)
public class TwilioAudioStreamWebsocketResource {

  private static final Logger LOG = LoggerFactory.getLogger(
    TwilioAudioStreamWebsocketResource.class
  );

  private StreamingSpeechToTextService streamingSpeechToTextService;
  private ObjectMapper objectMapper;

  private Session session;

  @Inject
  public TwilioAudioStreamWebsocketResource(
    StreamingSpeechToTextService streamingSpeechToTextService,
    ObjectMapper objectMapper
  ) {
    this.streamingSpeechToTextService = streamingSpeechToTextService;
    this.objectMapper = objectMapper;
  }

  @OnOpen
  public void myOnOpen(final Session session) throws IOException {
    LOG.info(
      "[sessionId: {}] Websocket session connection opened: {}",
      session.getId(),
      session
    );
    session.getAsyncRemote().sendText("Ready to receive live transcription results");
    this.session = session;
  }

  @OnMessage
  public void myOnMsg(final Session session, StreamMessage streamMessage) {
    streamingSpeechToTextService.handleStreamMessage(session, streamMessage);
  }

  @OnClose
  public void myOnClose(final Session session, CloseReason cr) {
    LOG.info("Closed connection! reason: {}, session: {}", cr, session);
    streamingSpeechToTextService.handleStreamClose(session);
  }
}
