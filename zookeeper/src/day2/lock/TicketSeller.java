package day2.lock;

public class TicketSeller {

    private void sell(){
        System.out.println("开始售票");
        int sleepMills=2000;
        try{
            //模拟复杂逻辑
            Thread.sleep(sleepMills);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("售票结束");
    }

    private void sellWithLock() throws Exception{
        ZKLock zkLock=new ZKLock();
        zkLock.acquireLock();
        sell();
        zkLock.releaseLock();
    }

    public static void main(String[] args) throws Exception {
        TicketSeller seller = new TicketSeller();
        for(int i=0;i<10;i++){
            seller.sellWithLock();
        }
    }
}
