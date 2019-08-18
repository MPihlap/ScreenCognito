#include "sys/time.h"

#include "BLEDevice.h"
#include "BLEUtils.h"
#include "BLEBeacon.h"
#include "esp_sleep.h"

#define GPIO_DEEP_SLEEP_DURATION     10  // sleep x seconds and then wake up
RTC_DATA_ATTR static time_t last;        // remember last boot in RTC Memory
RTC_DATA_ATTR static uint32_t bootcount; // remember number of boots in RTC Memory

BLEAdvertising *pAdvertising;

#define BEACON_UUID           "8ec76ea3-6668-48da-9866-75be8bc86f4d" // UUID 1 128-Bit (may use linux tool uuidgen or random numbers via https://www.uuidgenerator.net/)

int LED_BUILTIN = 2;
int LDR_PIN = 35;

int avgs[50];
int avg_index = 0;
int movementCounter = 0;

void setup(){
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  
  Serial.begin(115200);

  int val = analogRead(LDR_PIN);
  for(int i = 0; i < 50; i++){
    avgs[i] = val;
  }

  BLEDevice::init("AS");
  pAdvertising = BLEDevice::getAdvertising();
  setBeacon(0, 0);
  pAdvertising->start();
  Serial.println("Advertizing started...");
  delay(100);

  Serial.println("Setup done");
}

void loop(){
  int val = analogRead(LDR_PIN);
  avgs[avg_index] = val;

  avg_index++;
  if(avg_index >= 50) 
    avg_index = 0;
  
  int avg = 0;
  for (int i = 0; i < 50; i++){
    avg += avgs[i];
  }
  avg /= 50;

  int diff = avg-val;
  if(diff < 0)
    diff *= -1;

  if(diff > 10){
    movementCounter++;
  }else{
    movementCounter = 0;
  }
  
  Serial.print(val);
  Serial.print("   ");
  Serial.print(avg);
  Serial.print("   ");
  Serial.print(diff);
  Serial.println();

  digitalWrite(LED_BUILTIN, movementCounter<2?LOW:HIGH);
  if(movementCounter >= 2  &&  diff > 100){
    setBeacon(2, diff);
  }else if(movementCounter >= 2){
    setBeacon(1, diff);
  }else{
    setBeacon(0, diff);
  }
  
  delay(100);
}

void setBeacon(int minor, int major) {

  BLEBeacon oBeacon = BLEBeacon();
  oBeacon.setManufacturerId(0x4C00); // fake Apple 0x004C LSB (ENDIAN_CHANGE_U16!)
  oBeacon.setProximityUUID(BLEUUID(BEACON_UUID));
  oBeacon.setMajor(major);
  oBeacon.setMinor(minor);
  BLEAdvertisementData oAdvertisementData = BLEAdvertisementData();
  BLEAdvertisementData oScanResponseData = BLEAdvertisementData();
  
  oAdvertisementData.setFlags(0x04); // BR_EDR_NOT_SUPPORTED 0x04
  
  std::string strServiceData = "";
  
  strServiceData += (char)26;     // Len
  strServiceData += (char)0xFF;   // Type
  strServiceData += oBeacon.getData(); 
  oAdvertisementData.addData(strServiceData);
  
  pAdvertising->setAdvertisementData(oAdvertisementData);
  pAdvertising->setScanResponseData(oScanResponseData);

}
