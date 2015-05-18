package org.eclipse.paho.client.mqttv3;

/**
 * Created by guanxinquan on 15-5-18.
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
