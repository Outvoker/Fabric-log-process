package com.sdec.test;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * Log generate test demo
 * @author Xu Rui
 */
@Slf4j
public class Test {
    public static final int number = 10000;  //the number of logs contained by each file
    public static final String pushLogUrl = "http://localhost:28080/log/pushAsync";

    public static void main(String[] args) {
        HttpRequest push = new HttpRequest(pushLogUrl);
        LogGenerator logGenerator = new LogGenerator(push);
        long start = System.currentTimeMillis();
        try {
            long sendOver = logGenerator.generate(number);
            long end = System.currentTimeMillis();
            double time = ((double)(end - start)) / 1000;
            double dps = ((double)number) / time;
            log.info("Finish! start: {} time: {} dps: {} d/s", start, time, dps);
        }finally {
            logGenerator.shutdown();
        }
    }
}
