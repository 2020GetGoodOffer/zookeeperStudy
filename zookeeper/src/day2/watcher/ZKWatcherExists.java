package day2.watcher;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ZKWatcherExists {
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
    public void exist1() throws Exception{
        //第二个参数true表示使用zookeeper连接对象的监听器，就是main方法里的匿名内部类
        zooKeeper.exists("/watcher1",true);
        Thread.sleep(50000);
        System.out.println("结束");
    }

    @Test
    public void exist2() throws Exception{
        zooKeeper.exists("/watcher1", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("使用了自定义watcher");
                System.out.println("监听到的数据：");
                System.out.println("path = "+event.getPath());
                System.out.println("eventType = "+event.getType());
            }
        });
        Thread.sleep(50000);
        System.out.println("结束");
    }

    @Test
    public void exist3() throws Exception{
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    System.out.println("监听到的数据：");
                    System.out.println("path = "+event.getPath());
                    System.out.println("eventType = "+event.getType());
                    zooKeeper.exists("/watcher1",this);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        zooKeeper.exists("/watcher1",watcher);
        Thread.sleep(50000);
        System.out.println("结束");
    }

    @Test
    public void exist4() throws Exception{
        zooKeeper.exists("/watcher1", event -> {
            System.out.print("我是watcherA，");
            System.out.println("监听到了数据变化类型:"+event.getType());
        });
        zooKeeper.exists("/watcher1", event -> {
            System.out.print("我是watcherB，");
            System.out.println("监听到了数据变化类型:"+event.getType());
        });
        Thread.sleep(50000);
        System.out.println("结束");
    }
}
