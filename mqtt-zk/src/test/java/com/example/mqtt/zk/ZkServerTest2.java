package com.example.mqtt.zk;

import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;

/**
 * Created by guanxinquan on 15-5-11.
 */
public class ZkServerTest2 {

    public static final void main(String[] args) throws Exception {
        System.setProperty("zk","localhost:2181");
        IZkServer zkServer = ZkServerFactory.getInstance();
        zkServer.registerApiProvider(IZkServer.class.getTypeName(),"localhost",1088,null);

        try {
            while (true) {
                List<ChildData> data = zkServer.fetchApiProvider(IZkServer.class.getTypeName());

                for (ChildData d : data) {
                    System.out.println(d.getPath());
                }
                Thread.sleep(10000l);
            }
        }catch (Exception e){
            zkServer.close();
        }
    }

}
