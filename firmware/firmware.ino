#include "HX711.h"

float CALIB_CONST = 2280.f;
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

HX711 scale(A1, A0, 128);

void setup() {
  Serial.begin(38400);
  inputString.reserve(200);
  scale.set_scale(CALIB_CONST);
  scale.tare();
}

void loop() {
  
  Serial.print("W: ");
  Serial.println(scale.get_units(), 1);
  
  delay(100);
  
  if(stringComplete){
    if(inputString == "c"){
      scale.set_scale();
      scale.tare();   
      Serial.println("1000g calibrate");
      delay(5000);
      CALIB_CONST=scale.get_units(10)/1000.0;
      Serial.print("Calib. value: ");
      Serial.println(CALIB_CONST);
      Serial.println("Remove weight!");
      delay(2500);
      scale.set_scale(CALIB_CONST);
      scale.tare();
    } else if(inputString == "t"){
      scale.set_scale(CALIB_CONST);
      scale.tare();
      Serial.println("Tare!");
    }
    stringComplete=false;
    inputString="";
    delay(1000);
  }
  
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    
    if (inChar == '\n' || inChar == '\r') {
      stringComplete = true;
      inputString += "\0";
    } else {
      inputString += inChar;
    }
  }
}



