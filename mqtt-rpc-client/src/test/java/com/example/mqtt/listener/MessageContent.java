package com.example.mqtt.listener;

/**
 * Created by guanxinquan on 15-5-27.
 */
public class MessageContent {
    private Integer tplId;

    private String text;

    public MessageContent(Integer tplId, String text) {
        this.tplId = tplId;
        this.text = text;
    }

    public MessageContent() {
    }

    public Integer getTplId() {
        return tplId;
    }

    public void setTplId(Integer tplId) {
        this.tplId = tplId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
