package top.ryuu64.learn.spring.springmq.learnspringmq.consumer;

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
import top.ryuu64.learn.spring.springmq.learnspringmq.common.CrossServerMessage;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.PlayerLevelUpEvent;

import java.nio.charset.StandardCharsets;

/**
 * 跨服消息消费者
 * 使用 RocketMQ 4.x 标准客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrossServerConsumer {

    private final ObjectMapper objectMapper;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @Value("${rocketmq.consumer.group:game-consumer-group}")
    private String consumerGroup;

    private DefaultMQPushConsumer pushConsumer;

    /**
     * 跨服消息主题
     */
    private static final String TOPIC = "cross-server-events";

    @PostConstruct
    public void init() {
        try {
            pushConsumer = new DefaultMQPushConsumer(consumerGroup);
            pushConsumer.setNamesrvAddr(nameServer);
            pushConsumer.subscribe(TOPIC, "*");
            pushConsumer.setMessageListener(
                    (MessageListenerConcurrently) (messages, _) -> {
                        for (MessageExt message : messages) {
                            return handleMessage(message);
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
            );

            pushConsumer.start();
            log.info("跨服消息消费者启动成功 - Topic: {}, Group: {}", TOPIC, consumerGroup);

        } catch (Exception e) {
            log.error("初始化消费者失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (pushConsumer == null) {
            return;
        }

        pushConsumer.shutdown();
        log.info("消费者已关闭");
    }

    /**
     * 处理接收到的消息
     */
    private ConsumeConcurrentlyStatus handleMessage(MessageExt message) {
        try {
            String messageJson = new String(message.getBody(), StandardCharsets.UTF_8);
            String messageType = message.getTags();
            String messageId = message.getMsgId();

            log.info("收到跨服消息 - Tag: {}, Key: {}, MsgId: {}", messageType, message.getKeys(), messageId);

            CrossServerMessage crossServerMessage = objectMapper.readValue(messageJson, CrossServerMessage.class);
            handleMessageByType(crossServerMessage);

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        } catch (Exception e) {
            log.error("处理跨服消息失败: {}", e.getMessage(), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    /**
     * 根据消息类型处理消息
     */
    private void handleMessageByType(CrossServerMessage message) {
        switch (message.getMessageType()) {
            case "PLAYER_LEVEL_UP" -> handlePlayerLevelUp(message);
            case "ITEM_GAIN" -> handleItemGain(message);
            case "ACHIEVEMENT_COMPLETE" -> handleAchievementComplete(message);
            case "PLAYER_LOGIN" -> handlePlayerLogin(message);
            case "PLAYER_LOGOUT" -> handlePlayerLogout(message);
            case "LEADERBOARD_UPDATE" -> handleLeaderboardUpdate(message);
            default -> log.warn("未知的消息类型: {}", message.getMessageType());
        }
    }

    /**
     * 处理玩家升级事件
     */
    private void handlePlayerLevelUp(CrossServerMessage message) {
        try {
            PlayerLevelUpEvent event = objectMapper.readValue(message.getPayload(), PlayerLevelUpEvent.class);
            log.info("【处理玩家升级】玩家: {}, 从 {} 级升到 {} 级",
                    event.getPlayerName(), event.getOldLevel(), event.getNewLevel());
        } catch (Exception e) {
            log.error("处理玩家升级事件失败: {}", e.getMessage(), e);
        }
    }

    private void handleItemGain(CrossServerMessage message) {
        log.info("【处理获得物品】消息: {}", message.getPayload());
    }

    private void handleAchievementComplete(CrossServerMessage message) {
        log.info("【处理成就完成】消息: {}", message.getPayload());
    }

    private void handlePlayerLogin(CrossServerMessage message) {
        log.info("【处理玩家登录】消息: {}", message.getPayload());
    }

    private void handlePlayerLogout(CrossServerMessage message) {
        log.info("【处理玩家登出】消息: {}", message.getPayload());
    }

    private void handleLeaderboardUpdate(CrossServerMessage message) {
        log.info("【处理排行榜更新】消息: {}", message.getPayload());
    }
}
