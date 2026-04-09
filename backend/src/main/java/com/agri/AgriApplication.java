package com.agri;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 农业大数据联合建模平台应用程序入口
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.agri.mapper")
@ComponentScan("com.agri")
@EnableAsync
public class AgriApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriApplication.class, args);
    }

}