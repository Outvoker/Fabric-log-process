package org.fudan.logProcess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Xu Rui
 * @date 2021/1/25 14:44
 */
@SpringBootApplication
@EnableEurekaClient
public class LogIndexDataBaseMain {
    public static void main(String[] args) {
        SpringApplication.run(LogIndexDataBaseMain.class, args);
    }
}
