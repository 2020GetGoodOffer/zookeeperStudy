import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuratorDelete {
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
                .namespace("delete")
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
    public void del1() throws Exception{
        //删除结点
        client.delete()
                .forPath("/node1");

    }

    @Test
    public void del2() throws Exception{
        //使用版本号删除 -1代表不参与
        client.delete()
                .withVersion(-1)//不更新版本号
                .forPath("/node2");
    }


    @Test
    public void del3() throws Exception{
        //递归删除
        client.delete()
                .deletingChildrenIfNeeded()
                .forPath("/node3");
    }

    @Test
    public void del4() throws Exception{
        //异步修改
        client.delete()
                .withVersion(-1)
                .inBackground(new BackgroundCallback() {//异步回调方法
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                        System.out.println("结点路径"+curatorEvent.getPath());
                        System.out.println("事件类型"+curatorEvent.getType());
                    }
                })
                .forPath("/node4");
        Thread.sleep(5000);
    }
}
