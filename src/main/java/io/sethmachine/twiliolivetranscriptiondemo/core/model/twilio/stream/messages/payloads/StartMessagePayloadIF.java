package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages.payloads;

import com.hubspot.immutables.style.HubSpotStyle;
import io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.mediaformat.MediaFormat;
import java.util.List;
import java.util.Map;
import org.immutables.value.Value.Immutable;

@Immutable
@HubSpotStyle
public interface StartMessagePayloadIF {
  String getStreamSid();
  String getAccountSid();
  String getCallSid();
  List<String> getTracks();
  Map<String, String> getCustomParameters();

  MediaFormat getMediaFormat();
}
