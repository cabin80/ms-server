package com.ms.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ms.server.mapper")
public class MsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsServerApplication.class, args);
    }
}
