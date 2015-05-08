package com.example.mqtt.zk;

import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-8.
 */
public class ZkServerTest {

    public static void main(String[] args) throws InterruptedException {
        IZkServer server = ZkServerFactory.getInstance("localhost:2181");

        while(true) {
            List<ChildData> data = server.fetchServerPath();
            for (ChildData d : data) {
                System.out.println("path is "+d.getPath());
                System.out.println("data is "+new String(d.getData()));
            }
            Thread.sleep(10000);
        }


    }
}
