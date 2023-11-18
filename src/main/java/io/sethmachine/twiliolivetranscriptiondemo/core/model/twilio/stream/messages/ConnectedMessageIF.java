package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

import com.hubspot.immutables.style.HubSpotStyle;
import org.immutables.value.Value.Immutable;

@HubSpotStyle
@Immutable
// See: https://www.twilio.com/docs/voice/twiml/stream#message-connected
public interface ConnectedMessageIF extends StreamMessage {
  String getProtocol();
  String getVersion();
}
