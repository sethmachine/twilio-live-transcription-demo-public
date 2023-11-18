package io.sethmachine.twiliolivetranscriptiondemo.service.twilio.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.StreamMessage;
import java.util.Optional;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamMessageDecoder implements Decoder.Text<StreamMessage> {

  private static final Logger LOG = LoggerFactory.getLogger(StreamMessageDecoder.class);

  private ObjectMapper objectMapper;

  @Override
  public StreamMessage decode(String s) throws DecodeException {
    return decodeString(s)
      .orElseThrow(() -> {
        String msg = String.format("Failed to parse string into StreamMessage: %s", s);
        return new DecodeException(s, msg);
      });
  }

  @Override
  public boolean willDecode(String s) {
    return decodeString(s).isPresent();
  }

  @Override
  public void init(EndpointConfig config) {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void destroy() {}

  private Optional<StreamMessage> decodeString(String s) {
    try {
      return Optional.of(objectMapper.readValue(s, StreamMessage.class));
    } catch (Exception e) {
      LOG.error("Failed to decode string into StreamMessage: {}", s);
      return Optional.empty();
    }
  }
}
