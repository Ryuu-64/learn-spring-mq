package top.ryuu64.learn.spring.springmq.learnspringmq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Service;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.CrossServerMessage;

/**
 * 跨服消息生产者
 * 使用 RocketMQ 4.x 标准客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrossServerProducer {

    private final DefaultMQProducer producer;
    private final ObjectMapper objectMapper;

    /**
     * 跨服消息主题
     */
    private static final String TOPIC = "cross-server-events";

    @PostConstruct
    public void init() {
        try {
            producer.start();
            log.info("跨服消息生产者启动成功");
        } catch (Exception e) {
            log.error("启动生产者失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.shutdown();
            log.info("生产者已关闭");
        }
    }

    /**
     * 发送跨服消息
     */
    public void sendMessage(CrossServerMessage message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("序列化消息失败: {}", e.getMessage(), e);
            return;
        }

        try {
            Message rocketMessage = new Message(
                    TOPIC,
                    message.getMessageType(),
                    message.getMessageId(),
                    json.getBytes("UTF-8")
            );

            SendResult sendResult = producer.send(rocketMessage);

            log.info("发送跨服消息成功 - 类型: {}, 消息ID: {}, 来源: {}, 目标: {}, MessageId: {}",
                    message.getMessageType(),
                    message.getMessageId(),
                    message.getSourceService(),
                    message.getTargetService(),
                    sendResult.getMsgId());

        } catch (Exception e) {
            log.error("发送跨服消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送跨服消息（带自定义标签）
     */
    public void sendMessageWithTag(CrossServerMessage message, String tag) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("序列化消息失败: {}", e.getMessage(), e);
            return;
        }

        try {
            Message rocketMessage = new Message(
                    TOPIC,
                    tag,
                    message.getMessageId(),
                    json.getBytes("UTF-8")
            );

            producer.send(rocketMessage);

            log.info("发送跨服消息成功(带标签) - 类型: {}, Tag: {}, 消息ID: {}",
                    message.getMessageType(), tag, message.getMessageId());

        } catch (Exception e) {
            log.error("发送跨服消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送玩家升级事件
     */
    public void sendPlayerLevelUpEvent(Long playerId, String playerName,
                                       Integer oldLevel, Integer newLevel) {
        try {
            var event = new top.ryuu64.learn.spring.springmq.learnspringmq.common.PlayerLevelUpEvent(
                    playerId, playerName, oldLevel, newLevel, System.currentTimeMillis());
            String payload = objectMapper.writeValueAsString(event);

            CrossServerMessage message = CrossServerMessage.create(
                    "PLAYER_LEVEL_UP",
                    "game-service-a",
                    "game-service-b",
                    payload
            );

            sendMessage(message);
        } catch (JsonProcessingException e) {
            log.error("构建玩家升级事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送延迟消息
     */
    public void sendDelayMessage(CrossServerMessage message, long delayMillis) {
        try {
            String json = objectMapper.writeValueAsString(message);

            Message rocketMessage = new Message(
                    TOPIC,
                    message.getMessageType(),
                    message.getMessageId(),
                    json.getBytes("UTF-8")
            );

            producer.send(rocketMessage);
            log.info("发送消息 - 消息ID: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage(), e);
        }
    }
}
