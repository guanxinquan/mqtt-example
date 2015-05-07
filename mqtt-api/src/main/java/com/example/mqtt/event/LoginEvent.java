package com.example.mqtt.event;

/**
 * Created by guanxinquan on 15-5-7.
 *
 * 用户登录处理
 *
 */
public class LoginEvent extends MqttEvent {

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
