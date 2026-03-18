package top.ryuu64.learn.spring.springmq.learnspringmq.recharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.RechargeEvent;

import java.nio.charset.StandardCharsets;

/**
 * 日志消费者 - 负责记录所有充值操作的日志
 * 属于 log-group，独立消费充值成功消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final ObjectMapper objectMapper;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    private static final String CONSUMER_GROUP = "log-group";

    private DefaultMQPushConsumer pushConsumer;

    /**
     * 充值事件主题
     */
    private static final String TOPIC = "recharge-events";

    @PostConstruct
    public void init() {
        try {
            pushConsumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
            pushConsumer.setNamesrvAddr(nameServer);
            pushConsumer.subscribe(TOPIC, "RECHARGE_SUCCESS");
            pushConsumer.setMessageListener(
                    (MessageListenerConcurrently) (messages, _) -> {
                        for (MessageExt message : messages) {
                            return handleMessage(message);
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
            );

            pushConsumer.start();
            log.info("日志消费者启动成功 - Topic: {}, Group: {}", TOPIC, CONSUMER_GROUP);

        } catch (Exception e) {
            log.error("初始化日志消费者失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (pushConsumer == null) {
            return;
        }

        pushConsumer.shutdown();
        log.info("日志消费者已关闭");
    }

    private ConsumeConcurrentlyStatus handleMessage(MessageExt message) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            RechargeEvent event = objectMapper.readValue(json, RechargeEvent.class);

            // 记录充值操作日志
            log.info("【日志服务】玩家 {} 完成充值 - 订单号: {}, 金额: {} 元, 类型: {}, 时间: {}",
                    event.getPlayerId(),
                    event.getOrderId(),
                    event.getAmount(),
                    event.getRechargeType(),
                    event.getTimestamp());

            // 实际场景中会写入数据库或发送到大数据平台
            // rechargeLogRepository.save(buildLog(event));
            // elkService.sendLog("recharge", event);

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        } catch (Exception e) {
            log.error("【日志服务】处理充值消息失败: {}", e.getMessage(), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
