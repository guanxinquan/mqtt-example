package com.example.mqtt.event.listener;

/**
 * Created by guanxinquan on 15-5-27.
 * mqtt服务器通知dispatch服务器，同步用户状态。
 */
public class SyncUpEvent extends ListenerEvent{

    private String syncTag;

    public SyncUpEvent() {
    }

    public SyncUpEvent(String clientId,Long userId,String syncTag) {
        this.syncTag = syncTag;
        setClientID(clientId);
        setUserID(userId);
    }

    public String getSyncTag() {
        return syncTag;
    }

    public void setSyncTag(String syncTag) {
        this.syncTag = syncTag;
    }
}
