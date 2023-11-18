package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.mediaformat;

import com.hubspot.immutables.style.HubSpotStyle;
import org.immutables.value.Value.Immutable;

@Immutable
@HubSpotStyle
public interface MediaFormatIF {
  String getEncoding();
  String getSampleRate();
  String getChannels();
}
