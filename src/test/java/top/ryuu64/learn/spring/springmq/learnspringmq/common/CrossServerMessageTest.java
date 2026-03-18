package top.ryuu64.learn.spring.springmq.learnspringmq.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class CrossServerMessageTest {

    @Test
    void create_生成消息并填充字段() {
        CrossServerMessage message = CrossServerMessage.create(
                "PLAYER_LEVEL_UP",
                "game-service-a",
                "game-service-b",
                "{\"playerId\":1001}"
        );

        assertNotNull(message.getMessageId());
        assertEquals("PLAYER_LEVEL_UP", message.getMessageType());
        assertEquals("game-service-a", message.getSourceService());
        assertEquals("game-service-b", message.getTargetService());
        assertNotNull(message.getTimestamp());
        assertEquals("{\"playerId\":1001}", message.getPayload());
    }

    @Test
    void create_生成唯一消息ID() {
        CrossServerMessage msg1 = CrossServerMessage.create("TYPE_A", "src", "tgt", "payload");
        CrossServerMessage msg2 = CrossServerMessage.create("TYPE_A", "src", "tgt", "payload");

        assertNotNull(msg1.getMessageId());
        assertNotNull(msg2.getMessageId());
        assertNotEquals(msg1.getMessageId(), msg2.getMessageId());
    }

    @Test
    void create_时间戳为ISO格式() {
        CrossServerMessage message = CrossServerMessage.create("TYPE", "src", null, "payload");

        assertDoesNotThrow(() -> LocalDateTime.parse(message.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void create_targetService可以为null() {
        CrossServerMessage message = CrossServerMessage.create("TYPE", "src", null, "payload");

        assertNull(message.getTargetService());
    }

    @Test
    void 无参构造和setter_正常工作() {
        CrossServerMessage message = new CrossServerMessage();
        message.setMessageId("test-id");
        message.setMessageType("TEST_TYPE");
        message.setSourceService("test-source");
        message.setTargetService("test-target");
        message.setTimestamp("2026-03-18T12:00:00");
        message.setPayload("{}");

        assertEquals("test-id", message.getMessageId());
        assertEquals("TEST_TYPE", message.getMessageType());
        assertEquals("test-source", message.getSourceService());
        assertEquals("test-target", message.getTargetService());
        assertEquals("2026-03-18T12:00:00", message.getTimestamp());
        assertEquals("{}", message.getPayload());
    }
}
