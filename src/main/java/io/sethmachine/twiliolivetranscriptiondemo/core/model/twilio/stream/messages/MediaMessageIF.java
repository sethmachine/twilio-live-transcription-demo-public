package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hubspot.immutables.style.HubSpotStyle;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.payloads.MediaMessagePayload;
import org.immutables.value.Value.Immutable;

@HubSpotStyle
@Immutable
public interface MediaMessageIF extends StreamMessageCore {
  @JsonAlias("media")
  MediaMessagePayload getMediaMessagePayload();
}
