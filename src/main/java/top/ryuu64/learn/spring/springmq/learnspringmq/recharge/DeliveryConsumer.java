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
 * 发货消费者 - 负责给玩家发放虚拟货币/道具
 * 属于 delivery-group，消费充值成功消息并执行发货逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryConsumer {

    private final ObjectMapper objectMapper;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    private static final String CONSUMER_GROUP = "delivery-group";

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
            log.info("发货消费者启动成功 - Topic: {}, Group: {}", TOPIC, CONSUMER_GROUP);

        } catch (Exception e) {
            log.error("初始化发货消费者失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (pushConsumer == null) {
            return;
        }

        pushConsumer.shutdown();
        log.info("发货消费者已关闭");
    }

    private ConsumeConcurrentlyStatus handleMessage(MessageExt message) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            RechargeEvent event = objectMapper.readValue(json, RechargeEvent.class);

            // 模拟发货逻辑：根据充值金额发放钻石
            long diamonds = calculateDiamonds(event);
            log.info("【发货服务】玩家 {} 充值 {} 元，发放 {} 钻石，订单号: {}",
                    event.getPlayerId(), event.getAmount(), diamonds, event.getOrderId());

            // 实际场景中这里会调用游戏货币服务增加玩家钻石
            // gameCurrencyService.grantDiamonds(event.getPlayerId(), diamonds);

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        } catch (Exception e) {
            log.error("【发货服务】处理充值消息失败: {}", e.getMessage(), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    /**
     * 根据充值金额计算发放钻石数量
     */
    private long calculateDiamonds(RechargeEvent event) {
        return (long) (event.getAmount() * 10);
    }
}
