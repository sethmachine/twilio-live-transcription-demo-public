package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hubspot.immutables.style.HubSpotStyle;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.payloads.StopMessagePayload;
import org.immutables.value.Value.Immutable;

@HubSpotStyle
@Immutable
// See: https://www.twilio.com/docs/voice/twiml/stream#example-stop-message
public interface StopMessageIF extends StreamMessageCore {
  @JsonAlias("stop")
  StopMessagePayload getStopMessagePayload();
}
