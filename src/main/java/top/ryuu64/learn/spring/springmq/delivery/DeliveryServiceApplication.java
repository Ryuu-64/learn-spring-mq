package top.ryuu64.learn.spring.springmq.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import top.ryuu64.learn.spring.springmq.learnspringmq.recharge.DeliveryConsumer;

/**
 * 发货服务 - 只启动 DeliveryConsumer
 * 使用 application-delivery.yml 配置
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "top.ryuu64.learn.spring.springmq",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                top.ryuu64.learn.spring.springmq.learnspringmq.recharge.LogConsumer.class,
                                top.ryuu64.learn.spring.springmq.learnspringmq.recharge.StatisticsConsumer.class
                        }
                )
        }
)
public class DeliveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
    }
}
