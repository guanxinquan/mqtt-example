package com.example.mqtt.proto.messages;

/**
 * Created by guanxinquan on 15-5-5.
 */
public class ConnectMessage extends AbstractMessage{
    String protocolName;
    byte protocolVersion;

    //Connection flags
    boolean cleanSession;
    boolean willFlag;
    byte willQos;
    boolean willRetain;
    boolean passwordFlag;
    boolean userFlag;
    int keepAlive;

    //Variable part
    String username;
    String password;
    String clientID;
    String willTopic;
    String willMessage;

    public ConnectMessage() {
        messageType = CONNECT;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isPasswordFlag() {
        return passwordFlag;
    }

    public void setPasswordFlag(boolean passwordFlag) {
        this.passwordFlag = passwordFlag;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public boolean isUserFlag() {
        return userFlag;
    }

    public void setUserFlag(boolean userFlag) {
        this.userFlag = userFlag;
    }

    public boolean isWillFlag() {
        return willFlag;
    }

    public void setWillFlag(boolean willFlag) {
        this.willFlag = willFlag;
    }

    public byte getWillQos() {
        return willQos;
    }

    public void setWillQos(byte willQos) {
        this.willQos = willQos;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String topic) {
        this.willTopic = topic;
    }

    public String getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(String willMessage) {
        this.willMessage = willMessage;
    }




}
