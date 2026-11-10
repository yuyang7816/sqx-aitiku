package com.sqx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan(basePackages = {
        "com.sqx.system.**.mapper",
        "com.sqx.app.**.mapper"
})
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class SqxApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqxApplication.class, args);
        System.out.println("AI题库项目启动成功...");
    }
}
