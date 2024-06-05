#define FIREBASE_HOST       "wumul-bf62f-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH       "G3Gri1oQ72DPCVPflS1dqXSIMEhrtOxiUixOj4Ur"

#define WIFI_SSID           "gygy"
#define WIFI_PASSWORD       "12345678aaa"

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>

String    FIRE_NAME[2]   = {"sink", "head"};
String    str_data       = "";
String    str_info       = "";
int       get_int        = 0, get_flag = 0;
bool      serial_flag[2] = {false, false}, flag = false;

unsigned long prev_time;

void setup() {
  Serial.begin(9600);
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  
  Serial.println();  Serial.print("connected: ");
  Serial.println(WiFi.localIP());
  
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  
}

void loop() {
  Serial_read();

  if( (millis()-prev_time) >= 1000 ){
    read_flag();
    prev_time = millis();
  }

}

void read_flag(){
  get_flag = Firebase.getInt("flag");
  if( get_flag == 1 && !flag ){
    Serial.print("a");
    flag = true;
  }else if( get_flag == 0 && flag ){
    Serial.print("b");
    flag = false;
  }
}
void Serial_read(){
  if( Serial.available() ){
    char data = Serial.read();
    if(data == 'x'){
        for(int i=0;i<2;i++){
          if(serial_flag[i]){            
            get_int = Firebase.getInt(FIRE_NAME[i]);
            Firebase.setInt(FIRE_NAME[i], str_data.toInt()+get_int);
            str_info = FIRE_NAME[i]+","+str_data+","+String(get_int);
            str_data = "";
            Serial.println(str_info);
          }
          serial_flag[i] = false;
        }       
    }    
    if(serial_flag[0]||serial_flag[1])str_data+=data;
    if(data == 'a')serial_flag[0]=true;
    else if(data == 'b')serial_flag[1]=true;    
  }
}
