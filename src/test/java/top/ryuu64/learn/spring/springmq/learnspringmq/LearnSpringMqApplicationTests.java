package top.ryuu64.learn.spring.springmq.learnspringmq;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Spring Boot 上下文测试
 *
 * 注意：此类需要 RocketMQ 服务运行才能执行。
 * 如需测试上下文加载，请先启动 docker-compose up -d，
 * 然后移除 @Disabled 注解再运行。
 */
@Disabled("需要 RocketMQ 服务运行，单独运行单元测试时请跳过")
class LearnSpringMqApplicationTests {

    @Test
    void contextLoads() {
        // 启动完整的 Spring Boot 应用上下文
        // 需要 RocketMQ NameServer 运行
    }
}
