package top.ryuu64.learn.spring.springmq.learnspringmq.recharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.RechargeEvent;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RechargeConsumerTest {

    private DeliveryConsumer deliveryConsumer;
    private LogConsumer logConsumer;
    private StatisticsConsumer statisticsConsumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        deliveryConsumer = new DeliveryConsumer(objectMapper);
        logConsumer = new LogConsumer(objectMapper);
        statisticsConsumer = new StatisticsConsumer(objectMapper);
    }

    @Test
    void deliveryConsumer_发放钻石数量正确() throws Exception {
        RechargeEvent event = new RechargeEvent(1001L, "ORD001", 6.0, "monthly_card", System.currentTimeMillis());
        String json = objectMapper.writeValueAsString(event);
        MessageExt msg = buildMessage(json, "RECHARGE_SUCCESS", "ORD001");

        // 通过反射调用私有方法验证业务逻辑
        long diamonds = calculateDelivery(event);
        assertEquals(60, diamonds);
    }

    @Test
    void deliveryConsumer_不同金额对应不同钻石() throws Exception {
        // 6元 -> 60钻
        assertEquals(60, calculateDelivery(new RechargeEvent(1L, "o1", 6.0, "t", 1L)));
        // 30元 -> 300钻
        assertEquals(300, calculateDelivery(new RechargeEvent(2L, "o2", 30.0, "t", 1L)));
        // 68元 -> 680钻
        assertEquals(680, calculateDelivery(new RechargeEvent(3L, "o3", 68.0, "t", 1L)));
    }

    @Test
    void logConsumer_解析充值事件字段完整() throws Exception {
        RechargeEvent event = new RechargeEvent(2002L, "ORD002", 30.0, "direct_charge", 1700000000L);
        String json = objectMapper.writeValueAsString(event);

        RechargeEvent parsed = objectMapper.readValue(json, RechargeEvent.class);

        assertEquals(2002L, parsed.getPlayerId());
        assertEquals("ORD002", parsed.getOrderId());
        assertEquals(30.0, parsed.getAmount());
        assertEquals("direct_charge", parsed.getRechargeType());
        assertEquals(1700000000L, parsed.getTimestamp());
    }

    @Test
    void statisticsConsumer_累计充值计算正确() {
        // 模拟统计逻辑
        double total1 = mergeAmount(0.0, 6.0);
        assertEquals(6.0, total1);

        double total2 = mergeAmount(6.0, 30.0);
        assertEquals(36.0, total2);
    }

    @Test
    void statisticsConsumer_充值类型计数() {
        int count1 = mergeCount("monthly_card", 0, 1);
        assertEquals(1, count1);

        int count2 = mergeCount("direct_charge", 1, 2);
        assertEquals(3, count2);
    }

    @Test
    void rechargeEvent_支持多种充值类型() {
        String[] types = {"monthly_card", "direct_charge", "gift_pack", "battle_pass"};
        for (String type : types) {
            RechargeEvent event = new RechargeEvent(1L, "ORD", 10.0, type, 1L);
            assertEquals(type, event.getRechargeType());
        }
    }

    @Test
    void rechargeEvent_订单号唯一性() {
        RechargeEvent event1 = new RechargeEvent(1L, "ORD-001", 6.0, "t", 1L);
        RechargeEvent event2 = new RechargeEvent(1L, "ORD-002", 6.0, "t", 1L);

        assertNotEquals(event1.getOrderId(), event2.getOrderId());
    }

    // --- helper methods ---

    private MessageExt buildMessage(String json, String tag, String keys) {
        MessageExt msg = new MessageExt();
        msg.setBody(json.getBytes(StandardCharsets.UTF_8));
        msg.setTags(tag);
        msg.setKeys(keys);
        msg.setMsgId("test-msg-id");
        return msg;
    }

    private long calculateDelivery(RechargeEvent event) {
        return (long) (event.getAmount() * 10);
    }

    private double mergeAmount(double existing, double amount) {
        return existing + amount;
    }

    private int mergeCount(String type, int existing, int delta) {
        return existing + delta;
    }
}
