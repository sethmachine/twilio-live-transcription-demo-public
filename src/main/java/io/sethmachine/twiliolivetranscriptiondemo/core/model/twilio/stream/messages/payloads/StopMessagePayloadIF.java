package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.payloads;

import com.hubspot.immutables.style.HubSpotStyle;
import org.immutables.value.Value.Immutable;

@Immutable
@HubSpotStyle
public interface StopMessagePayloadIF {
  String getAccountSid();
  String getCallSid();
}
