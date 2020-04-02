package day1;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class zookeeperTest {

    private static final String IP="192.168.2.142:2181";
    private static ZooKeeper zooKeeper;
    @Before
    public void connect() throws Exception{
        //创建一个计数器对象
        CountDownLatch countDownLatch=new CountDownLatch(1);
        //第一个参数是服务器ip和端口号，第二个参数是客户端与服务器的会话超时时间单位ms，第三个参数是监视器对象
        zooKeeper=new ZooKeeper(IP, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getState()==Event.KeeperState.SyncConnected){
                    System.out.println("连接创建成功");
                    //通知主线程解除阻塞
                    countDownLatch.countDown();
                }
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
    public void create1() throws Exception{
        //同步创建结点
        // 参数1 结点路径
        // 参数2 结点数据
        // 参数3权限列表 OPEN_ACL_UNSAFE代表world方式授权 cdrwa
        // 参数4 结点类型 persistent表示持久化结点
        zooKeeper.create("/create/node1","i want offer".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void create2() throws Exception{
        //  OPEN_ACL_UNSAFE代表world方式授权 r只能读
        zooKeeper.create("/create/node2","i want offer".getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void create3() throws Exception{
        //自定义方式设置权限
        List<ACL> acls=new ArrayList<>();
        Id id=new Id("world","anyone");
        acls.add(new ACL(ZooDefs.Perms.CREATE,id));
        acls.add(new ACL(ZooDefs.Perms.DELETE,id));
        zooKeeper.create("/create/node3","i want offer".getBytes(), acls, CreateMode.PERSISTENT);
    }

    @Test
    public void create4() throws Exception{
        //ip方式设置权限
        List<ACL> acls=new ArrayList<>();
        Id id=new Id("ip","192.168.2.142");
        acls.add(new ACL(ZooDefs.Perms.CREATE,id));
        zooKeeper.create("/create/node4","i want offer".getBytes(), acls, CreateMode.PERSISTENT);
    }

    @Test
    public void create5() throws Exception{
        //auth方式设置权限
        zooKeeper.addAuthInfo("digest","sjh:sjh2019.".getBytes());
        zooKeeper.create("/create/node5","i want offer".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
    }

    @Test
    public void create6() throws Exception{
        //digest方式设置权限
        List<ACL> acls=new ArrayList<>();
        Id id=new Id("digest","sjh:base64和sha1加密后的密码");
        acls.add(new ACL(ZooDefs.Perms.CREATE,id));
        zooKeeper.create("/create/node6","i want offer".getBytes(), acls, CreateMode.PERSISTENT);
    }

    @Test
    public void create7() throws Exception{
        //持久化有序结点
        String s = zooKeeper.create("/create/node7", "i want offer".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        System.out.println(s);
    }

    @Test
    public void create8() throws Exception{
        //创建临时结点
        String s = zooKeeper.create("/create/node8", "i want offer".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println(s);
    }

    @Test
    public void create9() throws Exception{
        //创建临时结点
        String s = zooKeeper.create("/create/node9", "i want offer".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(s);
    }

    @Test
    public void create10() throws Exception{
        //异步创建结点
        zooKeeper.create("/create/node11", "i want offer".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                System.out.println("创建状态: "+rc);//0表示创建成功
                System.out.println("path: "+path);//结点路径
                System.out.println("name: "+name);//结点路径
                System.out.println("ctx: "+ctx);//上下文
            }
        },"context");
        Thread.sleep(1000);
    }

    @Test
    public void set1() throws Exception{
        //同步更新结点
        //第一个参数 结点路径
        //第二个参数 要修改的值
        //第三个参数 数据版本 -1代表版本号不参与更新
        zooKeeper.setData("/create/node1", "2020GetGoodOffer".getBytes(), -1);
    }

    @Test
    public void set2() throws Exception{
        //异步更新结点
        //第一个参数 结点路径
        //第二个参数 要修改的值
        //第三个参数 数据版本 -1代表版本号不参与更新
        //第四个参数 匿名回调函数
        //第五个参数 上下文参数
        zooKeeper.setData("/create/node1", "2020 Get Offer！！！".getBytes(), -1, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                System.out.println("rc: "+rc);//0表示成功
                System.out.println("path: "+path);
                System.out.println("ctx: "+ctx);
                System.out.println("stat: "+stat);
            }
        },"context");
        Thread.sleep(1000);
    }

    @Test
    public void del1() throws Exception{
        //同步删除数据
        //第一个参数表示删除结点的路径
        //第二个参数表示删除结点的数据版本 -1表示删除时不考虑版本信息
        zooKeeper.delete("/create/node1",-1);
    }

    @Test
    public void del2() throws Exception{
        //异步删除数据
        zooKeeper.delete("/create/node2", -1, new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                System.out.println("rc: "+rc);//0表示成功
                System.out.println("path: "+path);
                System.out.println("ctx: "+ctx);
            }
        },"context");
        Thread.sleep(1000);
    }

    @Test
    public void get1() throws Exception{
        //同步读取数据
        //第一个参数是路径
        //第二个参数是watch,先填false（以后在讲）
        //第三个参数用于获取结点属性
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData("/create/node3", false, stat);
        System.out.println(new String(data));
        System.out.println(stat);
    }

    @Test
    public void get2() throws Exception{
        //异步读取数据
        //第一个参数是路径
        //第二个参数是watch,先填false（以后在讲）
        //第三个参数是匿名回调函数
        Stat stat = new Stat();
        zooKeeper.getData("/create/node3", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("rc: "+rc);//0表示成功
                System.out.println("path: "+path);//结点路径
                System.out.println("ctx: "+ctx);//上下文参数
                System.out.println(new String(data));//数据
                System.out.println(stat);//属性
            }
        },"context");
        Thread.sleep(1000);
    }

    @Test
    public void getChild1() throws Exception{
        //同步
        //第一个参数是父路径
        //第二个参数是watch,先填false（以后在讲）
        List<String> children = zooKeeper.getChildren("/create/father", false);
        for(String str:children)
            System.out.println(str);
    }

    @Test
    public void getChild2() throws Exception{
        //异步
        //第一个参数是父路径
        //第二个参数是watch,先填false（以后在讲）
        //第三个参数是匿名回调函数
        zooKeeper.getChildren("/create/father", false, new AsyncCallback.ChildrenCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                System.out.println("rc: "+rc);//0表示成功
                System.out.println("path: "+path);//结点路径
                System.out.println("ctx: "+ctx);//上下文参数
                System.out.println(children);
            }
        },"context");
        Thread.sleep(1000);
    }

    @Test
    public void exists1() throws Exception{
        //同步判断
        //第一个参数是路径
        //第二个参数是watch,先填false（以后在讲）
        Stat exists = zooKeeper.exists("/create/null", false);
        System.out.println(exists==null?"不存在":"存在");
    }

    @Test
    public void exists2() throws Exception{
        //异步判断
        //第一个参数是路径
        //第二个参数是watch,先填false（以后在讲）
        //第三个参数是匿名回调函数
        //第四个参数上下文
        zooKeeper.exists("/create/null", false, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                System.out.println("rc: "+rc);//0表示成功
                System.out.println("path: "+path);//结点路径
                System.out.println("ctx: "+ctx);//上下文参数
                System.out.println(stat==null?"不存在":"存在");
            }
        },"context");
        Thread.sleep(1000);
    }
}
