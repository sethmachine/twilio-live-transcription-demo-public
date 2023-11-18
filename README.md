# Twilio Live Transcription Demo

## Introduction 

This repository runs a Java web server capable of handing incoming phone calls from Twilio and outputting live transcription results from Google Cloud Speech To Text in real time.  The server uses WebSockets to handle Twilio Media Streams and sending audio byte payloads to Google Cloud for transcription.  Once transcribed, the live results are published over another WebSocket connection for visualization and display.  

To try the demo, you will need the following accounts or tools installed:

* [A Twilio account](https://www.twilio.com/try-twilio)
* [A Google Cloud account](https://cloud.google.com/free)
* [IntelliJ Java IDE or equivalent](https://www.jetbrains.com/idea/download/)
* [Maven](https://maven.apache.org/)
* [ngrok](https://ngrok.com/docs/getting-started/)
* [PieSocket WebSocket Tester](https://chrome.google.com/webstore/detail/piesocket-websocket-teste/oilioclnckkoijghdniegedkbocfpnip)


