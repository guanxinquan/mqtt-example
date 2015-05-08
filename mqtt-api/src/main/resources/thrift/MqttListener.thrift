namespace java com.example.mqtt.api

service MqttListener{

    void disconnect(1:string clientID,2:i64 userID)

    bool login(1:string clientID,2:i64 userID,3:string password)

    void publish(1:string clientID,2:i64 userID,3:string paload,4:string topic)


}