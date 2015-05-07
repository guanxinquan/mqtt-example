package com.example.mqtt.server;

/**
 * Created by guanxinquan on 15-5-5.
 */
public interface ServerChannel {
    Object getAttribute(Object key);

    void setAttribute(Object key, Object value);

    void setIdleTime(int idleTime);

    void close(boolean immediately);

    void write(Object value);
}
