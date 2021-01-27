package org.fudan.logProcess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Xu Rui
 * @date 2021/1/26 22:04
 */
@SpringBootApplication
@EnableFeignClients
public class LogConsumerMain {
    public static void main(String[] args) {
        SpringApplication.run(LogConsumerMain.class, args);
    }
}
