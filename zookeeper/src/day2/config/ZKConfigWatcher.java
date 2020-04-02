package day2.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

//配置中心案例
public class ZKConfigWatcher implements Watcher {
    private CountDownLatch countDownLatch=new CountDownLatch(1);
    //zookeeper信息
    private static final String IP="192.168.2.142:2181";
    private static ZooKeeper zooKeeper;
    //数据库配置
    private String url;
    private String user;
    private String password;

    public ZKConfigWatcher(){
        //创建时初始化
        initValue();
    }

    @Override
    public void process(WatchedEvent event) {
        try{
            if(event.getType()== Event.EventType.None){
                if(event.getState()== Event.KeeperState.SyncConnected){
                    System.out.println("连接创建成功");
                    countDownLatch.countDown();
                }else if(event.getState()==Event.KeeperState.Disconnected)
                    System.out.println("断开连接");
                else if(event.getState()== Event.KeeperState.Expired) {
                    System.out.println("会话超时");
                    zooKeeper=new ZooKeeper(IP,5000,new ZKConfigWatcher());
                } else if(event.getState()== Event.KeeperState.AuthFailed) {
                    System.out.println("认证失败");
                }
            }else if(event.getType()== Event.EventType.NodeDataChanged){//结点的数据变化时
                initValue();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initValue() {
        try{
            //连接zookeeper
            if(zooKeeper==null)
                zooKeeper=new ZooKeeper(IP,5000,this);
            //阻塞线程等待连接创建成功
            countDownLatch.await();
            this.url=new String(zooKeeper.getData("/config/url", true, null));
            this.user=new String(zooKeeper.getData("/config/user", true, null));
            this.password=new String(zooKeeper.getData("/config/password", true, null));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ZKConfigWatcher zkConfigWatcher = new ZKConfigWatcher();
        for(int i=1;i<=10;i++){
            Thread.sleep(2000);
            System.out.println("当前url："+zkConfigWatcher.getUrl());
            System.out.println("当前user："+zkConfigWatcher.getUser());
            System.out.println("当前password："+zkConfigWatcher.getPassword());
            System.out.println("--------------");
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
