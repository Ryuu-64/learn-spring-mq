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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统计消费者 - 负责汇总充值数据，生成统计报表
 * 属于 statistics-group，独立消费充值成功消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsConsumer {

    private final ObjectMapper objectMapper;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    private static final String CONSUMER_GROUP = "statistics-group";

    private DefaultMQPushConsumer pushConsumer;

    /**
     * 充值事件主题
     */
    private static final String TOPIC = "recharge-events";

    /**
     * 模拟统计存储：玩家充值总额
     */
    private final Map<Long, Double> playerRechargeTotal = new ConcurrentHashMap<>();

    /**
     * 模拟统计存储：充值笔数
     */
    private final Map<String, Integer> rechargeTypeCount = new ConcurrentHashMap<>();

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
            log.info("统计消费者启动成功 - Topic: {}, Group: {}", TOPIC, CONSUMER_GROUP);

        } catch (Exception e) {
            log.error("初始化统计消费者失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (pushConsumer == null) {
            return;
        }

        pushConsumer.shutdown();
        log.info("统计消费者已关闭");
    }

    private ConsumeConcurrentlyStatus handleMessage(MessageExt message) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            RechargeEvent event = objectMapper.readValue(json, RechargeEvent.class);

            // 更新统计数据
            double total = playerRechargeTotal.merge(event.getPlayerId(), event.getAmount(), Double::sum);
            int count = rechargeTypeCount.merge(event.getRechargeType(), 1, Integer::sum);

            log.info("【统计服务】玩家 {} 充值统计 - 累计充值: {} 元 | {} 类型充值 {} 笔",
                    event.getPlayerId(), total, event.getRechargeType(), count);

            // 实际场景中会写入数据库或发送到 BI 系统
            // statisticsRepository.updatePlayerTotal(event.getPlayerId(), event.getAmount());
            // biService.reportRecharge(event);

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        } catch (Exception e) {
            log.error("【统计服务】处理充值消息失败: {}", e.getMessage(), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
