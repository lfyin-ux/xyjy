package com.xyjy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园社交平台启动类
 */
@SpringBootApplication
@MapperScan("com.xyjy.mapper")
public class XyjyApplication {
    public static void main(String[] args) {
        SpringApplication.run(XyjyApplication.class, args);
    }
}
