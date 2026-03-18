package top.ryuu64.learn.spring.springmq.logservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import top.ryuu64.learn.spring.springmq.learnspringmq.recharge.DeliveryConsumer;
import top.ryuu64.learn.spring.springmq.learnspringmq.recharge.StatisticsConsumer;

/**
 * 日志服务 - 只启动 LogConsumer
 * 使用 application-log.yml 配置
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "top.ryuu64.learn.spring.springmq",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                DeliveryConsumer.class,
                                StatisticsConsumer.class
                        }
                )
        }
)
public class LogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogServiceApplication.class, args);
    }
}
