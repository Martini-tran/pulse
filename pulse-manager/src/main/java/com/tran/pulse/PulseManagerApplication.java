package com.tran.pulse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * ...
 * pulse 项目启动类
 *
 * @version 1.0.0.0
 * @date 2025/3/24 16:58
 * @Author tran
 **/
@SpringBootApplication(scanBasePackages = "com.tran.pulse.*")
@MapperScan(basePackages = {
        "com.tran.pulse.*.mapper"
    })
@EnableWebSecurity  // 由使用方决定是否启用
public class PulseManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulseManagerApplication .class,args);
    }

}
