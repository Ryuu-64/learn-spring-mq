package top.ryuu64.learn.spring.springmq.learnspringmq.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 跨服消息基类
 * 所有跨服务通信的消息都继承这个基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossServerMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 消息类型：PLAYER_LEVEL_UP, ITEM_GAIN, ACHIEVEMENT_COMPLETE 等
     */
    private String messageType;

    /**
     * 发送方服务名称
     */
    private String sourceService;

    /**
     * 目标服务名称（可选，用于消息过滤）
     */
    private String targetService;

    /**
     * 发送时间
     */
    private LocalDateTime timestamp;

    /**
     * 消息内容（JSON 格式）
     */
    private String payload;

    public static CrossServerMessage create(String messageType, String sourceService,
                                            String targetService, String payload) {
        CrossServerMessage message = new CrossServerMessage();
        message.setMessageId(java.util.UUID.randomUUID().toString());
        message.setMessageType(messageType);
        message.setSourceService(sourceService);
        message.setTargetService(targetService);
        message.setTimestamp(LocalDateTime.now());
        message.setPayload(payload);
        return message;
    }
}
