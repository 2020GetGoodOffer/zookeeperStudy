package day2.uniqueID;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

//分布式唯一ID案例
public class ZKUniqueIDWatcher implements Watcher {
    private CountDownLatch countDownLatch=new CountDownLatch(1);
    //zookeeper信息
    private static final String IP="192.168.2.142:2181";
    private static ZooKeeper zooKeeper;
    //唯一ID
    private String uniqueId="/id";

    public ZKUniqueIDWatcher(){
        try{
            //连接zookeeper
            if(zooKeeper==null)
                zooKeeper=new ZooKeeper(IP,5000,this);
            //阻塞线程等待连接创建成功
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        try{
            if(event.getType()== Event.EventType.None) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("连接创建成功");
                    countDownLatch.countDown();
                } else if (event.getState() == Event.KeeperState.Disconnected)
                    System.out.println("断开连接");
                else if (event.getState() == Event.KeeperState.Expired) {
                    System.out.println("会话超时");
                    zooKeeper = new ZooKeeper(IP, 5000, new ZKUniqueIDWatcher());
                } else if (event.getState() == Event.KeeperState.AuthFailed) {
                    System.out.println("认证失败");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getUniqueId(){
        try{
            //创建临时有序结点
            return zooKeeper.create(uniqueId, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        ZKUniqueIDWatcher zkUniqueIDWatcher= new ZKUniqueIDWatcher();
        for(int i=1;i<=10;i++){
            System.out.println(zkUniqueIDWatcher.getUniqueId());
        }
    }


}
