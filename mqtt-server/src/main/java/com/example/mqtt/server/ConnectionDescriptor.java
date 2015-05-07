package com.example.mqtt.server;

/**
 * Created by guanxinquan on 15-5-6.
 */
public class ConnectionDescriptor {

    private String clientID;

    private ServerChannel session;

    public ConnectionDescriptor(String clientID, ServerChannel session) {
        this.clientID = clientID;
        this.session = session;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public ServerChannel getSession() {
        return session;
    }

    public void setSession(ServerChannel session) {
        this.session = session;
    }
}
