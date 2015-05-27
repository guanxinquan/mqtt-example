package com.example.mqtt.listener;

/**
 * Created by guanxinquan on 15-5-27.
 */
public class UserModel {

    private Integer type;

    private Long userId;

    private String name;

    public UserModel(Integer type, Long userId, String name) {
        this.type = type;
        this.userId = userId;
        this.name = name;
    }

    public UserModel() {
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
