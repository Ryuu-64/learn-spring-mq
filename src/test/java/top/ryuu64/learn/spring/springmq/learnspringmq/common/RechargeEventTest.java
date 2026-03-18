package top.ryuu64.learn.spring.springmq.learnspringmq.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RechargeEventTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void 全参构造_字段正确赋值() {
        RechargeEvent event = new RechargeEvent(
                1001L, "ORD001", 6.0, "monthly_card", 1700000000L
        );

        assertEquals(1001L, event.getPlayerId());
        assertEquals("ORD001", event.getOrderId());
        assertEquals(6.0, event.getAmount());
        assertEquals("monthly_card", event.getRechargeType());
        assertEquals(1700000000L, event.getTimestamp());
    }

    @Test
    void 无参构造_字段为null或零值() {
        RechargeEvent event = new RechargeEvent();

        assertNull(event.getPlayerId());
        assertNull(event.getOrderId());
        assertNull(event.getAmount());
        assertNull(event.getRechargeType());
        assertNull(event.getTimestamp());
    }

    @Test
    void setter_正常赋值() {
        RechargeEvent event = new RechargeEvent();
        event.setPlayerId(2002L);
        event.setOrderId("ORD002");
        event.setAmount(30.0);
        event.setRechargeType("direct_charge");
        event.setTimestamp(1800000000L);

        assertEquals(2002L, event.getPlayerId());
        assertEquals("ORD002", event.getOrderId());
        assertEquals(30.0, event.getAmount());
        assertEquals("direct_charge", event.getRechargeType());
        assertEquals(1800000000L, event.getTimestamp());
    }

    @Test
    void json序列化_可正常序列化() throws Exception {
        RechargeEvent event = new RechargeEvent(1001L, "ORD001", 6.0, "monthly_card", 1700000000L);

        String json = objectMapper.writeValueAsString(event);

        assertNotNull(json);
        assertTrue(json.contains("\"playerId\":1001"));
        assertTrue(json.contains("\"orderId\":\"ORD001\""));
        assertTrue(json.contains("\"amount\":6.0"));
        assertTrue(json.contains("\"rechargeType\":\"monthly_card\""));
    }

    @Test
    void json反序列化_可正常反序列化() throws Exception {
        String json = "{\"playerId\":1001,\"orderId\":\"ORD001\",\"amount\":6.0,\"rechargeType\":\"monthly_card\",\"timestamp\":1700000000}";

        RechargeEvent event = objectMapper.readValue(json, RechargeEvent.class);

        assertEquals(1001L, event.getPlayerId());
        assertEquals("ORD001", event.getOrderId());
        assertEquals(6.0, event.getAmount());
        assertEquals("monthly_card", event.getRechargeType());
        assertEquals(1700000000L, event.getTimestamp());
    }

    @Test
    void json序列化再反序列化_数据一致() throws Exception {
        RechargeEvent original = new RechargeEvent(3003L, "ORD003", 68.0, "gift_pack", 1900000000L);

        String json = objectMapper.writeValueAsString(original);
        RechargeEvent restored = objectMapper.readValue(json, RechargeEvent.class);

        assertEquals(original.getPlayerId(), restored.getPlayerId());
        assertEquals(original.getOrderId(), restored.getOrderId());
        assertEquals(original.getAmount(), restored.getAmount());
        assertEquals(original.getRechargeType(), restored.getRechargeType());
        assertEquals(original.getTimestamp(), restored.getTimestamp());
    }
}
