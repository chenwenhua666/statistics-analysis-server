package com.plm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author cwh
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class StatisticsAnalysisServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsAnalysisServerApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉ启动成功");
    }
}
