import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CuratorWatcher {
    private static final String IP="192.168.2.142:2181,192.168.2.142:2182,192.168.2.142:2183";
    private CuratorFramework client;

    @Before
    public void connect(){
        //重连机制
        RetryPolicy retryPolicy=new ExponentialBackoffRetry(1000,3);
        //创建连接
        client= CuratorFrameworkFactory.builder()
                .connectString(IP)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        //打开连接
        client.start();
        System.out.println("连接创建成功");
    }

    @After
    public void close(){
        client.close();
    }


    @Test
    public void watcher1() throws Exception{
        //监视某个结点 arg1 连接对象 arg2 监视路径
        NodeCache nodeCache=new NodeCache(client,"/watcher1");
        //启动监视器
        nodeCache.start();
        System.out.println("监视器已打开");
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("结点路径： "+nodeCache.getCurrentData().getPath());
            }
        });
        Thread.sleep(150000);
        //关闭监视器
        nodeCache.close();
        System.out.println("监视器已关闭");

    }

    @Test
    public void watcher2() throws Exception{
        //监视子结点 arg1 连接对象 arg2 监视路径 arg3 能否读取数据
        PathChildrenCache pathChildrenCache=new PathChildrenCache(client,"/watcher1",true);
        //启动监视器
        pathChildrenCache.start();
        System.out.println("监视器已打开");
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) {
                System.out.println("结点事件类型 ："+pathChildrenCacheEvent.getType());
                System.out.println("结点的路径： "+pathChildrenCacheEvent.getData().getPath());
                System.out.println("结点数据： "+new String(pathChildrenCacheEvent.getData().getData()));
            }
        });
        Thread.sleep(150000);
        //关闭监视器
        pathChildrenCache.close();
        System.out.println("监视器已关闭");
    }



}
