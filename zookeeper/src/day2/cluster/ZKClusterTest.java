package day2.cluster;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

//连接集群
public class ZKClusterTest {

    public static void main(String[] args) {
        CountDownLatch countDownLatch=new CountDownLatch(1);
        try{
            ZooKeeper zooKeeper=new ZooKeeper("192.168.2.142:2181,192.168.2.142:2182,192.168.2.142:2183", 5000, event -> {
                if(event.getState()== Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("连接集群成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            zooKeeper.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
