import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CuratorCreate {
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
                .namespace("create")
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
    public void create1() throws Exception{
        //新增结点
        client.create()
                //持久化结点
                .withMode(CreateMode.PERSISTENT)
                //权限列表为 world:anyone:cdrwa
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                //arg1 结点路径 arg2 结点数据 由于指定了命名空间 会创建/create/node1
                .forPath("/node1","2020GetGoodOffer".getBytes());

    }

    @Test
    public void create2() throws Exception{
        //自定义权限列表
        List<ACL> list=new ArrayList<>();
        Id id=new Id("ip","192.168.2.142");
        list.add(new ACL(ZooDefs.Perms.ALL,id));
        //新增结点
        client.create()
                .withMode(CreateMode.PERSISTENT)
                .withACL(list)
                .forPath("/node2","2020GetGoodOffer".getBytes());
    }

    @Test
    public void create3() throws Exception{
        //递归创建
       client.create()
                .creatingParentsIfNeeded()//如果父路径不存在则创建
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath("/node3/node31","2020GetGoodOffer".getBytes());
    }

    @Test
    public void create4() throws Exception{
        //异步创建
        client.create()
                .creatingParentsIfNeeded()//如果父路径不存在则创建
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .inBackground(new BackgroundCallback() {//异步回调方法
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                        System.out.println("结点路径"+curatorEvent.getPath());
                        System.out.println("事件类型"+curatorEvent.getType());
                    }
                })
                .forPath("/node4","2020GetGoodOffer".getBytes());
        Thread.sleep(5000);
    }
}
