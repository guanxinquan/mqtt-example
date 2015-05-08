package com.example.mqtt.zk;

import org.apache.curator.framework.recipes.cache.ChildData;

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by guanxinquan on 15-5-8.
 */
public interface IZkServer extends Closeable{

    public List<ChildData> fetchServerPath();

    public AtomicInteger getServerDateVersion();

    public AtomicInteger getClientDateVersion();

    public void registerServerPath(String path,byte[] data);

    public void registerClientPath(String path,byte[] data);


}
