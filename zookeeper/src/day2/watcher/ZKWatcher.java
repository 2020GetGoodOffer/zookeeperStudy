package day2.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class ZKWatcher implements Watcher {

    //创建计数器对象
    static CountDownLatch countDownLatch=new CountDownLatch(1);
    //创建连接对象
    static ZooKeeper zooKeeper;

    public static void main(String[] args) {
        try{
            zooKeeper=new ZooKeeper("192.168.2.142:2181",5000,new ZKWatcher());
            //阻塞线程等待连接创建
            countDownLatch.await();
            zooKeeper.addAuthInfo("digest","kobe:123456".getBytes());
            zooKeeper.getData("/tmp", false, null);
            Thread.sleep(5000);
            zooKeeper.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        try{
            if(event.getType()== Event.EventType.None){
                if(event.getState()==Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功");
                    //解除线程阻塞
                    countDownLatch.countDown();
                } else if(event.getState()==Event.KeeperState.Disconnected)
                    System.out.println("断开连接");
                else if(event.getState()== Event.KeeperState.Expired)
                    System.out.println("会话超时");
                else if(event.getState()== Event.KeeperState.AuthFailed) {
                    System.out.println("认证失败");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
