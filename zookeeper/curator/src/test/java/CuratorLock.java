import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuratorLock {
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
    public void lock1() throws Exception{
        //排它锁
        InterProcessLock interProcessLock=new InterProcessMutex(client,"/lock1");
        System.out.println("等待获取锁对象");
        interProcessLock.acquire();
        for(int i=1;i<=10;i++){
            Thread.sleep(3000);
            System.out.println(i);
        }
        interProcessLock.release();
        System.out.println("等待释放锁");
    }

    @Test
    public void lock2() throws Exception{
        //读写锁
        InterProcessReadWriteLock interProcessReadWriteLock=new InterProcessReadWriteLock(client,"/lock1");
        InterProcessMutex interProcessLock = interProcessReadWriteLock.readLock();
        System.out.println("等待读锁对象");
        interProcessLock.acquire();
        for(int i=1;i<=10;i++){
            Thread.sleep(3000);
            System.out.println(i);
        }
        interProcessLock.release();
        System.out.println("等待释放锁");
    }

    @Test
    public void lock3() throws Exception{
        //读写锁
        InterProcessReadWriteLock interProcessReadWriteLock=new InterProcessReadWriteLock(client,"/lock1");
        InterProcessMutex interProcessLock = interProcessReadWriteLock.writeLock();
        System.out.println("等待写锁对象");
        interProcessLock.acquire();
        for(int i=1;i<=10;i++){
            Thread.sleep(3000);
            System.out.println(i);
        }
        interProcessLock.release();
        System.out.println("等待释放锁");
    }



}
