package io.sethmachine.twiliolivetranscriptiondemo.core.model.speech.google;

import com.hubspot.immutables.style.HubSpotStyle;
import org.immutables.value.Value.Immutable;

@HubSpotStyle
@Immutable
public interface TranscriptOutputMessageIF {
  String getText();
  float getConfidence();
  boolean getIsFinal();
}
