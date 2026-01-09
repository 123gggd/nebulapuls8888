package com.nebula.commerce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync // [合并] 开启异步任务支持 (原 NebulaApplication 中的注解)
@EnableScheduling // [关键] 开启定时任务，否则 OrderTask 不会执行
@EnableTransactionManagement // [关键] 开启事务支持
@MapperScan("com.nebula.commerce.modules.*.mapper") // 扫描所有模块的 Mapper
public class NebulaCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NebulaCommerceApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  星云商城后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}