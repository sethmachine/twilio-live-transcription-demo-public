package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hubspot.immutables.style.HubSpotStyle;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.payloads.StartMessagePayload;
import org.immutables.value.Value.Immutable;

@HubSpotStyle
@Immutable
// See: https://www.twilio.com/docs/voice/twiml/stream#message-start
public interface StartMessageIF extends StreamMessageCore {
  @JsonAlias("start")
  StartMessagePayload getStartMessagePayLoad();
}
