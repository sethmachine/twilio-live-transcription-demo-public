package io.sethmachine.twiliolivetranscriptiondemo.core.model.twilio.stream.messages;

public interface StreamMessageCore extends StreamMessage {
  String getSequenceNumber();
  String getStreamSid();
}
