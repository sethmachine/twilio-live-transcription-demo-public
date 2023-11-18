package io.sethmachine.twiliolivetranscriptiondemo;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import io.sethmachine.twiliolivetranscriptiondemo.guice.GuiceWebsocketConfigurator;
import io.sethmachine.twiliolivetranscriptiondemo.guice.TwilioLiveTranscriptionDemoModule;
import io.sethmachine.twiliolivetranscriptiondemo.resources.TwilioAudioStreamWebsocketResource;
import io.sethmachine.twiliolivetranscriptiondemo.service.twilio.stream.StreamMessageDecoder;
import javax.websocket.server.ServerEndpointConfig;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class TwilioLiveTranscriptionDemoApplication
  extends Application<TwilioLiveTranscriptionDemoConfig> {

  public static void main(final String[] args) throws Exception {
    new TwilioLiveTranscriptionDemoApplication().run(args);
  }

  @Override
  public String getName() {
    return "TwilioLiveTranscriptionDemo";
  }

  @Override
  public void initialize(final Bootstrap<TwilioLiveTranscriptionDemoConfig> bootstrap) {
    // required to access private fields in the Encoding class
    bootstrap
      .getObjectMapper()
      .setVisibility(
        VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.ANY)
      );

    GuiceBundle guiceBundle = GuiceBundle
      .builder()
      .enableAutoConfig(getClass().getPackage().getName())
      .modules(new TwilioLiveTranscriptionDemoModule())
      .build();
    bootstrap.addBundle(guiceBundle);

    // NOTE: supplier is required to allow for lazy initialization of the guice injection
    final ServerEndpointConfig config = ServerEndpointConfig.Builder
      .create(TwilioAudioStreamWebsocketResource.class, "/twilio/websocket/audio-stream")
      .configurator(new GuiceWebsocketConfigurator(() -> guiceBundle.getInjector()))
      .decoders(ImmutableList.of(StreamMessageDecoder.class))
      .build();
    bootstrap.addBundle(new WebsocketBundle(config));
  }

  @Override
  public void run(
    final TwilioLiveTranscriptionDemoConfig configuration,
    final Environment environment
  ) {}
}
