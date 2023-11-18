package io.sethmachine.twiliolivetranscriptiondemo.core.concurrent.speech.google;

import javax.websocket.Session;

public interface StreamingSpeechToTextRunnableFactory {
  StreamingSpeechToTextRunnable create(Session websocketSession);
}
