namespace java com.example.mqtt.api

service MqttServer{

   void publishByUserId(1:i64 userId,2:string payload,3:string topic)

   void publishByClientId(1:string clientId,2:i64 userId,3:string payload,4:string topic)
}