package day2.watcher;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ZKWatcherGetChildren {
    private static final String IP="192.168.2.142:2181";
    private static ZooKeeper zooKeeper;
    //创建一个计数器对象
    CountDownLatch countDownLatch=new CountDownLatch(1);

    @Before
    public void connect() throws Exception{
        //第一个参数是服务器ip和端口号，第二个参数是客户端与服务器的会话超时时间单位ms，第三个参数是监视器对象
        zooKeeper=new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getState()==Event.KeeperState.SyncConnected){
                    System.out.println("连接创建成功");
                    //通知主线程解除阻塞
                    countDownLatch.countDown();
                }
                System.out.println("监听到的数据：");
                System.out.println("path = "+event.getPath());
                System.out.println("eventType = "+event.getType());
            }
        });
        //主线程阻塞，等待连接对象的创建成功
        countDownLatch.await();
    }

    @After
    public void close() throws Exception{
        zooKeeper.close();
    }

    @Test
    public void getChildren1() throws Exception{
        //true表示复用zookeeper连接对象的watcher
        zooKeeper.getChildren("/watcher1",true);
        Thread.sleep(50000);
    }

    @Test
    public void getChildren2() throws Exception{
        //使用自定义watcher
        zooKeeper.getChildren("/watcher1", event -> {
            System.out.println("监听到的数据：");
            System.out.println("path = "+event.getPath());
            System.out.println("eventType = "+event.getType());
        });
        Thread.sleep(50000);
    }

    @Test
    public void getChildren3() throws Exception{
        //实现多次注册
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    System.out.println("监听到的数据：");
                    System.out.println("path = " + event.getPath());
                    System.out.println("eventType = " + event.getType());
                    //事件类型是数据变化时再注册watcher继续监听
                    if(event.getType()== Event.EventType.NodeChildrenChanged)
                        zooKeeper.getChildren("/watcher1",this);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        zooKeeper.getChildren("/watcher1",watcher);
        Thread.sleep(50000);
    }

    @Test
    public void getChildren4() throws Exception{
        //注册多个watcher
        zooKeeper.getChildren("/watcher1", event -> {
            System.out.print("我是watcherA，");
            System.out.println("监听到了数据变化类型:"+event.getType());
        });
        zooKeeper.getChildren("/watcher1", event -> {
            System.out.print("我是watcherB，");
            System.out.println("监听到了数据变化类型:"+event.getType());
        });
        Thread.sleep(50000);
    }
}
