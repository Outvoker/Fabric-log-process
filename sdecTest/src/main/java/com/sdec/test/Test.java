package com.sdec.test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log generate test demo
 * @author Xu Rui
 */
public class Test {
    public static final int number = 1000;  //the number of logs contained by each file
    public static final String pushLogUrl = "http://localhost:28080/log/push";

    public static void main(String[] args) {
        HttpRequest push = new HttpRequest(pushLogUrl);
        LogGenerator logGenerator = new LogGenerator(push);
        long start = System.currentTimeMillis();
        try {
            int success = logGenerator.generate(number);
            long end = System.currentTimeMillis();
            System.out.println("Finish! time: " + (end - start) +  " success " + success);
        }finally {
            logGenerator.shutdown();
        }
    }
}
