package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "event",
  visible = true
)
@JsonSubTypes(
  {
    @JsonSubTypes.Type(value = ConnectedMessage.class, name = "connected"),
    @JsonSubTypes.Type(value = StartMessage.class, name = "start"),
    @JsonSubTypes.Type(value = MediaMessage.class, name = "media"),
    @JsonSubTypes.Type(value = StopMessage.class, name = "stop"),
  }
)
public interface StreamMessage {
  @JsonAlias("event")
  MessageEventType getMessageEventType();
}
