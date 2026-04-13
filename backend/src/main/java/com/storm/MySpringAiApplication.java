package com.storm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//默认不扫描mapper包!!!!!
@MapperScan("com.storm.mapper")
@SpringBootApplication
public class MySpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringAiApplication.class, args);
    }

}
