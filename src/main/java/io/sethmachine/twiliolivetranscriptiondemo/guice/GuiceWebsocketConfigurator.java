package io.sethmachine.twiliolivetranscriptiondemo.guice;

import com.google.inject.Injector;
import java.util.function.Supplier;
import javax.websocket.server.ServerEndpointConfig;

public class GuiceWebsocketConfigurator extends ServerEndpointConfig.Configurator {

  private final Supplier<Injector> injectorSupplier;

  public GuiceWebsocketConfigurator(Supplier<Injector> injectorSupplier) {
    this.injectorSupplier = injectorSupplier;
  }

  public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
    return injectorSupplier.get().getInstance(endpointClass);
  }
}
