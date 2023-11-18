package io.sethmachine.twiliolivetranscriptiondemo.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import io.dropwizard.Configuration;
import io.sethmachine.twiliolivetranscriptiondemo.core.concurrent.speech.google.StreamingSpeechToTextRunnableFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

public class TwilioLiveTranscriptionDemoModule
  extends DropwizardAwareModule<Configuration> {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(StreamingSpeechToTextRunnableFactory.class));

    configuration();
    environment();
    bootstrap();
  }

  @Provides
  @Singleton
  @Named("StreamingCloudSpeechToTextThreadPoolExecutor")
  public ThreadPoolExecutor provideThreadPoolExecutorForCloudSpeechToText() {
    return new ThreadPoolExecutor(
      8,
      100,
      60,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue()
    );
  }

  @Provides
  @Singleton
  public ObjectMapper provideObjectMapper() {
    return bootstrap().getObjectMapper();
  }
}
