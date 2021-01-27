package org.fudan.logProcess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application
 * @author Xu Rui
 */
@SpringBootApplication
@EnableAsync
public class LogProducerMain {
    public static void main(String[] args) {
        SpringApplication.run(LogProducerMain.class, args);
    }
}
