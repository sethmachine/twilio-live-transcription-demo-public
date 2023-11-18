package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MessageEventType {
  CONNECTED("connected"),
  START("start"),
  MEDIA("media"),
  STOP("stop");

  private static final Map<String, MessageEventType> EVENT_TO_ENUM_MAP = Arrays
    .stream(MessageEventType.values())
    .collect(
      Collectors.toUnmodifiableMap(MessageEventType::getEventName, Function.identity())
    );
  private final String eventName;

  MessageEventType(String eventName) {
    this.eventName = eventName;
  }

  @JsonValue
  public String getEventName() {
    return eventName;
  }

  @JsonCreator
  public static MessageEventType fromEventName(String eventName) {
    MessageEventType maybeEntry = EVENT_TO_ENUM_MAP.get(eventName);
    if (Objects.isNull(maybeEntry)) {
      throw new IllegalArgumentException(
        String.format("Unknown value for MessageEventType enum: %s", eventName)
      );
    }
    return maybeEntry;
  }
}
