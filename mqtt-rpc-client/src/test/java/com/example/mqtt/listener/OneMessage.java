package com.example.mqtt.listener;

/**
 * Created by guanxinquan on 15-5-27.
 */
public class OneMessage {
    private Integer type;

    private UserModel from;

    private UserModel to;

    private MessageContent content;

    public OneMessage(Integer type, UserModel from, UserModel to, MessageContent content) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.content = content;
    }

    public OneMessage() {
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public UserModel getFrom() {
        return from;
    }

    public void setFrom(UserModel from) {
        this.from = from;
    }

    public UserModel getTo() {
        return to;
    }

    public void setTo(UserModel to) {
        this.to = to;
    }

    public MessageContent getContent() {
        return content;
    }

    public void setContent(MessageContent content) {
        this.content = content;
    }

}
