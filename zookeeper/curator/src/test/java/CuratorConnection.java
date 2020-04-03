import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

public class CuratorConnection {

    public static void main(String[] args) throws Exception {
        //重连策略
        //3秒后重连1次
        RetryPolicy r1 = new RetryOneTime(3000);
        //每3秒重连1次，一共3次
        RetryPolicy r2 = new RetryNTimes(3, 3000);
        //每3秒重连1次，总时间10秒
        RetryPolicy r3 = new RetryUntilElapsed(10000, 3000);
        //随着重连次数增加，重连间隔变长（基于第一个参数计算）
        RetryPolicy r4 = new ExponentialBackoffRetry(1000, 3);

        //创建连接对象
        CuratorFramework client= CuratorFrameworkFactory.builder()
                .connectString("192.168.2.142:2181,192.168.2.142:2182,192.168.2.142:2183")//集群的ip地址和端口号
                .sessionTimeoutMs(5000)//超时时间
                .retryPolicy(new RetryOneTime(3000))//重连机制 超时3秒后重连一次
                .namespace("create")//指定命名空间
                .build();//构建连接
        //打开连接
        client.start();
        System.out.println(client.isStarted()?"连接集群成功":"连接失败");
        //关闭连接
        client.close();
    }
}
