package com.example.mqtt.event.listener;

/**
 * Created by guanxinquan on 15-5-11.
 * 用户登录事件
 */
public class LoginEvent extends ListenerEvent {

    private String password;

    public LoginEvent(Long userId,String clientId,String password) {
        this.password = password;
        setUserID(userId);
        setClientID(clientId);
    }

    public LoginEvent(){

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
