package top.ryuu64.learn.spring.springmq.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import top.ryuu64.learn.spring.springmq.learnspringmq.recharge.DeliveryConsumer;
import top.ryuu64.learn.spring.springmq.learnspringmq.recharge.LogConsumer;

/**
 * 统计服务 - 只启动 StatisticsConsumer
 * 使用 application-statistics.yml 配置
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "top.ryuu64.learn.spring.springmq",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                DeliveryConsumer.class,
                                LogConsumer.class
                        }
                )
        }
)
public class StatisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}
