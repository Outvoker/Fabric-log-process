package com.sdec.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvokeOneByOneTest {
    public static final int number = 10000;  //the number of logs contained by each file
    public static final String pushLogUrl = "http://localhost:21001/fabric/pushLog";

    public static void main(String[] args) {
        HttpRequest push = new HttpRequest(pushLogUrl);
        LogGenerator logGenerator = new LogGenerator(push);
        long start = System.currentTimeMillis();
        try {
            long sendOver = logGenerator.generate(number);
            long end = System.currentTimeMillis();
            double time = ((double)(end - start)) / 1000;
            double dps = ((double)number) / time;
            double rps = ((double)number) / (((double)(sendOver - start)) / 1000);
            log.info("Finish! time: {} sendOver {} rps: {} r/s dps: {} d/s", time, sendOver, rps, dps);
        }finally {
            logGenerator.shutdown();
        }
    }
}
