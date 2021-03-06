package com.t8webs.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableCaching
@EnableRetry
public class T8WebsApplication {

    public static void main(String[] args) {
        SpringApplication.run(T8WebsApplication.class, args);
    }

}
