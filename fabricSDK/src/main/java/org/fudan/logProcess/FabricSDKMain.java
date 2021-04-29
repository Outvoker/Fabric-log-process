package org.fudan.logProcess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
public class FabricSDKMain {
    public static void main(String[] args) {
        SpringApplication.run(FabricSDKMain.class, args);
    }
}
