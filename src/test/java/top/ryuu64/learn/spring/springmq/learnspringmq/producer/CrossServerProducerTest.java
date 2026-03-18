package top.ryuu64.learn.spring.springmq.learnspringmq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.CrossServerMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrossServerProducerTest {

    @Mock
    private DefaultMQProducer producer;

    private ObjectMapper objectMapper;
    private CrossServerProducer crossServerProducer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        crossServerProducer = new CrossServerProducer(producer, objectMapper);
    }

    @Test
    void sendMessage_发送跨服消息成功() throws Exception {
        SendResult mockResult = new SendResult();
        mockResult.setSendStatus(SendStatus.SEND_OK);
        mockResult.setMsgId("mock-msg-id");
        when(producer.send(any(Message.class))).thenReturn(mockResult);

        CrossServerMessage message = CrossServerMessage.create(
                "PLAYER_LEVEL_UP",
                "game-service-a",
                "game-service-b",
                "{\"playerId\":1001}"
        );
        crossServerProducer.sendMessage(message);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer, times(1)).send(captor.capture());

        Message sent = captor.getValue();
        assertEquals("cross-server-events", sent.getTopic());
        assertEquals("PLAYER_LEVEL_UP", sent.getTags());
    }

    @Test
    void sendRechargeEvent_发送充值事件成功() throws Exception {
        SendResult mockResult = new SendResult();
        mockResult.setSendStatus(SendStatus.SEND_OK);
        mockResult.setMsgId("mock-recharge-msg-id");
        when(producer.send(any(Message.class))).thenReturn(mockResult);

        crossServerProducer.sendRechargeEvent(1001L, "ORD001", 6.0, "monthly_card");

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer, times(1)).send(captor.capture());

        Message sent = captor.getValue();
        assertEquals("recharge-events", sent.getTopic());
        assertEquals("RECHARGE_SUCCESS", sent.getTags());
        assertEquals("ORD001", sent.getKeys());

        String body = new String(sent.getBody());
        assertTrue(body.contains("\"playerId\":1001"));
        assertTrue(body.contains("\"orderId\":\"ORD001\""));
        assertTrue(body.contains("\"amount\":6.0"));
        assertTrue(body.contains("\"rechargeType\":\"monthly_card\""));
    }

    @Test
    void sendRechargeEvent_充值金额小数正确() throws Exception {
        SendResult mockResult = new SendResult();
        mockResult.setSendStatus(SendStatus.SEND_OK);
        mockResult.setMsgId("mock-msg");
        when(producer.send(any(Message.class))).thenReturn(mockResult);

        crossServerProducer.sendRechargeEvent(2002L, "ORD002", 68.88, "direct_charge");

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer).send(captor.capture());

        String body = new String(captor.getValue().getBody());
        assertTrue(body.contains("\"amount\":68.88"));
    }

    @Test
    void sendMessage_verify发送内容和Topic正确() throws Exception {
        SendResult mockResult = new SendResult();
        mockResult.setSendStatus(SendStatus.SEND_OK);
        mockResult.setMsgId("msg-123");
        when(producer.send(any(Message.class))).thenReturn(mockResult);

        CrossServerMessage message = CrossServerMessage.create(
                "ITEM_GAIN",
                "game-service-a",
                null,
                "{\"itemId\":999}"
        );
        crossServerProducer.sendMessage(message);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer).send(captor.capture());

        Message sent = captor.getValue();
        assertEquals("cross-server-events", sent.getTopic());
        assertEquals("ITEM_GAIN", sent.getTags());
        // payload 在 CrossServerMessage 中是 String 类型，JSON 中会被转义
        assertTrue(new String(sent.getBody()).contains("\"payload\":\"{\\\"itemId\\\":999}"));
    }
}
