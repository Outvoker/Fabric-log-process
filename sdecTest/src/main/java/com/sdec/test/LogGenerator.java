package com.sdec.test;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * log generator
 *
 * @author Xu Rui
 */
@Slf4j
public class LogGenerator {
    public static final String charStr = "0123456789abcdefghijklmnopqrstuvwxyz";
    public static final Integer EXID_LENGTH = 32;
    public static final Integer demanderID = 1;

    /**
     * thread pool : used to concurrent send post request
     */
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(2000);

    private HttpRequest httpRequest;

    void shutdown() {
        threadPool.shutdown();
    }

    LogGenerator(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    LogGenerator() {
    }

    public long generate(int number) {
        AtomicInteger send = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger success = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(number);

        SimpleDateFormat myDate = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat myTime = new SimpleDateFormat("HHmmss");
        String curDate = myDate.format(System.currentTimeMillis());
        String curTime = myTime.format(System.currentTimeMillis());


        int[] suppliers = new int[7];
        int k = 0;
        for (int i = 1; i <= 8; i++) {
            if (demanderID.equals(i))
                continue;
            suppliers[k++] = i;
        }

        int count = 1;
        while (count <= number) {
            StringBuilder sb = new StringBuilder();


            int index = (int) (Math.random() * suppliers.length);
            int supplierID = suppliers[index];
//                String exid = getCharAndNum(EXID_LENGTH);                    //随机生成exid
            supplierID = count % 6 + 2;

            int exid = count;                    //随机生成exid
            String taskID = "CTN201712150000" + demanderID + supplierID;  //生成taskID
            sb.append(count).append("|@|")    //自增序号
                    .append("21").append("|@|")  //流程状态
                    .append(curDate).append("|@|")  //日期
                    .append(curTime).append("|@|")   //时间
                    .append(demanderID).append("|@|")  //需方会员
                    .append(supplierID).append("|@|")  //供方会员
                    .append(taskID).append("|@|")     //taskID
                    .append(curDate + "_" + curTime).append("|@|")  //业务流水号
                    .append(exid).append("|@|")                   //exid
                    .append("0002|@|2|@|1|@|0|@|031010|@|3|@|1|@|0000149|@|1|@||@|2|@||@|1|@||@|3|@|0000149|@|1|@|");

            count++;

            // simulate the transaction log generate rate
//                try {
//                    TimeUnit.MILLISECONDS.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            // thread pool: concurrent post
            threadPool.execute(() -> {

                if (httpRequest != null) {
                    String s = null;
                    try {
                        log.info("send: {}", send.incrementAndGet());
                        s = httpRequest.sendPost(sb.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.info("failed: {}", failed.incrementAndGet());
                        return;
                    }
//                    System.out.println(s);
                    log.info("success: {}", success.incrementAndGet());
                } else log.info("failed: {}", failed.incrementAndGet());
                countDownLatch.countDown();
            });
        }

        long sendOver = System.currentTimeMillis();
        log.info("send over: {}", sendOver);


        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sendOver;
    }

    public static String getCharAndNum(int length) {
        Random random = new Random();
        StringBuilder valSb = new StringBuilder();
        int charLength = charStr.length();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charLength);
            valSb.append(charStr.charAt(index));
        }
        return valSb.toString();
    }

    public static void main(String[] args) {
        LogGenerator logGenerator = new LogGenerator();
        logGenerator.generate(2000);
    }

}
