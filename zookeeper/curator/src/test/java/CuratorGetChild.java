import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CuratorGetChild {
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
                .namespace("getChild")
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
    public void getChild1() throws Exception{
        //读取结点
        List<String> list = client.getChildren()
                .forPath("/node1");
        System.out.println(list);
    }

    @Test
    public void getChild2() throws Exception{
        //读取数据时读取结点属性
        Stat stat=new Stat();
        client.getChildren()
                .storingStatIn(stat)
                .forPath("/node2");
    }



    @Test
    public void getChild3() throws Exception{
        //异步读取
        client.getChildren()
                .inBackground(new BackgroundCallback() {//异步回调方法
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                        System.out.println("结点路径"+curatorEvent.getPath());
                        System.out.println("事件类型"+curatorEvent.getType());
                    }
                })
                .forPath("/node3");
        Thread.sleep(5000);
    }
}
