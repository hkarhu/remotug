int z_button = 5;
int kg_button = 6;
int sens = A0;

float kilo_ref = 82;
float kilo = 1;
int zero = 0;

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
  Serial.begin(9600);
  pinMode(z_button, INPUT);
  pinMode(kg_button, INPUT);
  //Serial.println("start");
  
  // reserve 200 bytes for the inputString:
  inputString.reserve(200);
}

void loop() {
 
  Serial.println(analogRead(sens));
  Serial.println(measure());
  
  delay(100);
  
  if(stringComplete){
    if(inputString == "q"){
      // set 'ref' kilogram
      kilo = analogRead(sens)/kilo_ref;
      Serial.println("reference saved");
    } else if(inputString == "w"){
      // set zero
      zero = analogRead(sens);
      Serial.println("zero saved");
    }
    stringComplete=false;
    inputString="";
    delay(1000);
  }
  
}

float measure() {
  int val = analogRead(sens);
  return (float)(val-zero)/(float)(kilo-zero);
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



