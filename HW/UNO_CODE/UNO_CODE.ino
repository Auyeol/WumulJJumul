
#include <SoftwareSerial.h>

SoftwareSerial esp8266(2, 3);

//  YF_S201[0]  cool, YF_S201[0]  hot, YF_S201[2]  showerhead

const int       YF_S201[3]  = {8, 9, 10};
const float     CalibrationFactor = 4.5;

//  YF_count = 유량센서 트리거, FLAG_YF = high low구분, FlowRate 현재유량
int             YF_count[3] = {0, 0, 0};
bool            FLAG_YF[3]  = {true, true, true};
int             Finsh_Flow[3] = {0, 0, 0};
float           FlowRate[3] = {0, 0, 0};
String          str_Serial  = "";
char            char_start[3] = {'a', 'b'};

unsigned int    mL_FR[3]    = {0, 0, 0};
unsigned long   Total_FR[3] = {0, 0, 0};

unsigned long   prev_time;

//bool            send_flag[3] = {false, false, false};
bool            send_flag = false, start_flag = false;
unsigned long   send_time;

void setup() {  
  Serial.begin(9600);
  esp8266.begin(9600);
  Serial.println("UNO_CODE");
  for(int i=0;i<3;i++)pinMode(YF_S201[i], INPUT_PULLUP);  

}

void loop() {
  if(start_flag){
    read_yf_s201();  
    if( (millis() - prev_time) >= 1000){
      for(int i=0;i<3;i++){
        FlowRate[i] = ((1000.0/(millis()-prev_time)) * YF_count[i])/CalibrationFactor;  
  
        mL_FR[i] = (FlowRate[i]/60)*1000;
        Total_FR[i] += mL_FR[i];
  
        YF_count[i] = 0;
        Serial.println();
        Serial.print(i);                     Serial.print(". Flow rate : ");
        Serial.print(int(FlowRate[i]));      Serial.print(".");
    
        Serial.print("  Current Liquid Flowing: ");
        Serial.print(Total_FR[i]);           Serial.println("mL");
        Serial.println();
  
        if(FlowRate[i]>0)Finsh_Flow[i]=1;
        else if( (FlowRate[i]==0) && (Total_FR[i]>0) ){
          Finsh_Flow[i]=2;
          if(!send_flag)send_time = millis();
          send_flag = true;          
        }
      }    
      prev_time = millis();  
    }
  
    if(send_flag){
      if(Finsh_Flow[0]==2||Finsh_Flow[1]==2){
        str_Serial = char_start[0]+String(Total_FR[0]+Total_FR[1])+"x";
        Serial.println(str_Serial);        
        esp8266.print(str_Serial);
        Finsh_Flow[0]=0;        Finsh_Flow[1]=0;
        Total_FR[0] = 0;        Total_FR[1] = 0;
        send_flag = false;
      }else if(Finsh_Flow[2]==2){
        str_Serial = char_start[1]+String(Total_FR[2])+"x";
        Serial.println(str_Serial);        
        esp8266.print(str_Serial);
        Finsh_Flow[2]=0;
        Total_FR[2] = 0;
        send_flag = false;
      }
    }
    
  }
  

  if(esp8266.available()){
    char data = esp8266.read();
    if(data == 'a'){
      start_flag = true;
      reset_val();
      Serial.println("START_READ");
    }else if(data == 'b'){
      start_flag = false;
      reset_val();
      Serial.println("STOP_READ");
    }
  }

}

void read_yf_s201(){
  if(start_flag){
    for(int i=0;i<3;i++){
      if( (digitalRead(YF_S201[i]) == HIGH) && !FLAG_YF[i] )FLAG_YF[i]=true;
      else if( (digitalRead(YF_S201[i]) == LOW) && FLAG_YF[i] ){
        FLAG_YF[i] = false;
        YF_count[i]++;
      }  
    }  
  }   
}

void reset_val(){
  for(int i=0;i<3;i++){
    YF_count[i] = 0;
    FLAG_YF[i] = true;
    Finsh_Flow[i] =0;
    FlowRate[i] = 0;
    mL_FR[i] = 0;
    Total_FR[i] = 0;
  }  
  str_Serial  = "";  
  send_flag = false;
}
