
#define HANDSHAKE_ACK 66
#define MESSAGE_END '.'

String data = "";
boolean dataReady = false;

void setup() {
  Serial.begin(9600);
  
  while(Serial.available() <= 0) {
    Serial.print((char) HANDSHAKE_ACK);
    
    delay(300);
  }
}

void loop() {
  if(dataReady) {
    int operation = (int) data[0];
    
    switch(operation) {
      case 1 : {
        pinMode((int) data[1], (int) data[2]);
        
        break;
      }
      
      case 2 : {
        digitalWrite((int) data[1], (int) data[2]);
        
        break;
      }
      
      case 3 : {
        analogWrite((int) data[1], (int) data[2]);
        
        break;
      }
      
      default : {
        return;
      }
    }
    
    data = "";
    dataReady = false;
  }
}


void serialEvent() {
  while(Serial.available()) {
    char newData = (char) Serial.read();
    
     if(newData != HANDSHAKE_ACK) {
      data += newData;
      
      if(newData == MESSAGE_END) {
        dataReady = true;
      }
    }
  }
}
