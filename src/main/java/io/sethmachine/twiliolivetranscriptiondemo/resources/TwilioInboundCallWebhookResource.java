package io.sethmachine.twiliolivetranscriptiondemo.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/twilio/webhooks/inbound-call")
@Produces(MediaType.TEXT_XML)
@Consumes(MediaType.TEXT_XML)
public class TwilioInboundCallWebhookResource {

  private static final String WEBSOCKET_CONNECT_PATH = "twilio/websocket/audio-stream";

  @Inject
  public TwilioInboundCallWebhookResource() {}

  @GET
  public String getTwiml(@Context HttpHeaders httpHeaders) {
    String websocketUri = buildWebsocketUri(httpHeaders);

    return String.format(
      "    <Response>\n" +
      "      <Start>\n" +
      "        <Stream url=\"%s\"/>\n" +
      "      </Start>\n" +
      "      <Say>This calling is being recorded.  Streaming 60 seconds of audio for live transcription.</Say>\n" +
      "      <Pause length=\"60\" />\n" +
      "    </Response>",
      websocketUri
    );
  }

  private static String buildWebsocketUri(HttpHeaders httpHeaders) {
    String hostName = httpHeaders.getRequestHeader("Host").get(0);
    return String.format("wss://%s/%s", hostName, WEBSOCKET_CONNECT_PATH);
  }
}
