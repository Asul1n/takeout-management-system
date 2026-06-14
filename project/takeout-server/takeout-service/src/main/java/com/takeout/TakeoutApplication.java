package com.takeout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 外卖管理系统 — Spring Boot 启动类
 */
@SpringBootApplication(scanBasePackages = "com.takeout")
public class TakeoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(TakeoutApplication.class, args);
        System.out.println("============================================");
        System.out.println("  外卖管理系统 Takeout Server 启动成功！");
        System.out.println("  API 文档: http://localhost:8080/doc.html");
        System.out.println("============================================");
    }
}
