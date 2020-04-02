package day2.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

//分布式锁案例
public class ZKLock {
    //计数器对象
    private CountDownLatch countDownLatch=new CountDownLatch(1);
    //zookeeper信息
    private static ZooKeeper zooKeeper;
    private static final String IP="192.168.2.142:2181";
    private static final String LOCK_ROOT_PATH="/locks";
    private static final String LOCK_NODE_PATH="lock_";
    private String lockPath;
    //监视上一个结点是否被删除
    private Watcher watcher=new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if(event.getType()== Event.EventType.NodeDeleted){
                synchronized (this){
                    watcher.notifyAll();
                }
            }
        }
    };

    //在构造器中连接zookeeper
    public ZKLock(){
        try {
            zooKeeper=new ZooKeeper(IP, 5000, event -> {
                if(event.getType()== Watcher.Event.EventType.None) {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        System.out.println("连接创建成功");
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //获取锁
    public void acquireLock() throws Exception{
        //创建锁结点
        createLock();
        //尝试获取锁
        attemptLock();
    }

    //创建锁结点
    private void createLock() throws Exception{
        //判断locks是否存在,不存在则创建为持久化结点
        if (zooKeeper.exists(LOCK_ROOT_PATH, false)==null)
            zooKeeper.create(LOCK_ROOT_PATH,new byte[0],ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        //创建临时有序结点
        lockPath=zooKeeper.create(LOCK_ROOT_PATH+"/"+LOCK_NODE_PATH,new byte[0],ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("结点创建成功： "+lockPath);
    }

    //尝试获取锁
    private void attemptLock() throws Exception{
        //获取/locks下的所有子结点
        List<String> lockList = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
        //对子结点列表排序
        Collections.sort(lockList);
        //当前结点的位置
        int index=lockList.indexOf(lockPath.substring(LOCK_ROOT_PATH.length()+1));
        //是第一位
        if(index==0){
            System.out.println("获取锁成功");
        }else {
            //获取上一个结点的路径并监视
            String prePath=lockList.get(index-1);
            Stat stat = zooKeeper.exists(LOCK_ROOT_PATH + "/" + prePath, watcher);
            if(stat==null){
                attemptLock();
            }else {
                synchronized (this){
                    wait();
                }
                attemptLock();
            }
        }
    }

    //释放锁
    public void releaseLock() throws Exception{
        //删除临时有序结点
        zooKeeper.delete(lockPath,-1);
        zooKeeper.close();
        System.out.println(lockPath+" 锁已经释放");
    }


}

