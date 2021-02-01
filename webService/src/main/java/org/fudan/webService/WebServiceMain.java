package org.fudan.webService;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WebServiceMain {
    public static void main(String[] args){
        SpringApplication.run(WebServiceMain.class, args);
    }
}
