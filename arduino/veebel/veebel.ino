#include <NewPing.h>
#include <SPI.h>
#include <WiFiNINA.h>

///////please enter your sensitive data in the Secret tab/arduino_secrets.h
char ssid[] = "";        // your network SSID (name)
char pass[] = "";    // your network password (use for WPA, or use as key for WEP)
int keyIndex = 0;            // your network key Index number (needed only for WEP)
int counter = 0;
int status = WL_IDLE_STATUS;
String message; //string that stores the incoming message
char ch;

// Initialize the Wifi client library
WiFiClient client;

// server address:
//char server[] = "";
IPAddress server(90, 191, 160, 122);
WiFiServer server2(80);

unsigned long lastConnectionTime = 0;            // last time you connected to the server, in milliseconds
const unsigned long postingInterval = 10L * 1000L; // delay between updates, in milliseconds

NewPing sonar(3, 4, 200);

void setup() {
  Serial.begin(9600);
  Serial1.begin(9600);
  pinMode(A1, INPUT);
  pinMode(LED_BUILTIN, OUTPUT);

  digitalWrite(LED_BUILTIN, HIGH);

  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true);
  }

  String fv = WiFi.firmwareVersion();
  if (fv < "1.0.0") {
    Serial.println("Please upgrade the firmware");
  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin(ssid, pass);
  }
  // you're connected now, so print out the status:
  server2.begin();
  printWifiStatus();
  Serial1.println("AT+IBEA0");
  Serial1.println("AT+RESET");
  digitalWrite(LED_BUILTIN, LOW);
  // put your setup code here, to run once:

}

void loop() {
  if (Serial1.available())
  {
    Serial.write(Serial1.read());
  }
  // put your main code here, to run repeatedly:
  //Serial.println(sonar.ping_cm());
  //Serial.println(analogRead(A1));
  WiFiClient client2 = server2.available();
  if (client2) {                             // if you get a client,
    Serial.println("new client");           // print a message out the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client2.connected()) {            // loop while the client's connected
      if (client2.available()) {             // if there's bytes to read from the client,
        char c = client2.read();             // read a byte, then
        //Serial.write(c);                    // print it out the serial monitor
        if (c == '\n') {                    // if the byte is a newline character

          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client2.println("HTTP/1.1 200 OK");
            client2.println("Content-type:text/html");
            client2.println();

            // The HTTP response ends with another blank line:
            client.println();
            // break out of the while loop:
            break;
          } else {    // if you got a newline, then clear currentLine:
            currentLine = "";
          }
        } else if (c != '\r') {  // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }

        if (currentLine.endsWith("GET /1")) {
          Serial1.println("AT+IBEA1");
          Serial1.println("AT+RESET");
        }
        if (currentLine.endsWith("GET /0")) {
          Serial1.println("AT+IBEA0");
          Serial1.println("AT+RESET");
        }
      }
    }
    // close the connection:
    client2.stop();
    Serial.println("client disonnected");
  }

  while (client.available()) {
    char c = client.read();
    //Serial.write(c);
  }
  // if ten seconds have passed since your last connection,
  // then connect again and send data:
  if (millis() - lastConnectionTime > postingInterval) {
    digitalWrite(LED_BUILTIN, HIGH);
    httpRequest();
    counter++;

    //Serial1.println("AT+IBEA1");
    //Serial1.println("AT+RESET");
    digitalWrite(LED_BUILTIN, LOW);
  }
}

void httpRequest() {
  // close any connection before send a new request.
  // This will free the socket on the Nina module
  client.stop();

  // if there's a successful connection:
  if (client.connect(server, 80)) {
    Serial.println("Sending data");
    // send the HTTP PUT request:
    client.println("GET /web/sodi/screencognito/vahendaja.php?d=" + String(counter) + " HTTP/1.1");
    client.println("Host: example.org");
    client.println("User-Agent: ArduinoWiFi/1.1");
    client.println("Connection: close");
    client.println();

    // note the time that the connection was made:
    lastConnectionTime = millis();
  } else {
    // if you couldn't make a connection:
    Serial.println("connection failed");
  }
}


void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}
