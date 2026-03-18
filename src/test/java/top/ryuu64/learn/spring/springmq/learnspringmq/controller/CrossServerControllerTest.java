package top.ryuu64.learn.spring.springmq.learnspringmq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.CrossServerMessage;
import top.ryuu64.learn.spring.springmq.learnspringmq.producer.CrossServerProducer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrossServerControllerTest {

    @Test
    void sendLevelUpEvent_参数正确时返回成功() {
        CrossServerProducer producer = mock(CrossServerProducer.class);
        CrossServerController controller = new CrossServerController(producer);

        Map<String, Object> request = new HashMap<>();
        request.put("playerId", "1001");
        request.put("playerName", "张三");
        request.put("oldLevel", "10");
        request.put("newLevel", "11");

        Map<String, Object> result = controller.sendLevelUpEvent(request);

        assertTrue((Boolean) result.get("success"));
        assertEquals("玩家升级事件已发送", result.get("message"));
        verify(producer).sendPlayerLevelUpEvent(1001L, "张三", 10, 11);
    }

    @Test
    void sendLevelUpEvent_不同的oldLevel和newLevel() {
        CrossServerProducer producer = mock(CrossServerProducer.class);
        CrossServerController controller = new CrossServerController(producer);

        Map<String, Object> request = new HashMap<>();
        request.put("playerId", "2002");
        request.put("playerName", "李四");
        request.put("oldLevel", "1");
        request.put("newLevel", "99");

        controller.sendLevelUpEvent(request);

        verify(producer).sendPlayerLevelUpEvent(2002L, "李四", 1, 99);
    }

    @Test
    void sendMessage_发送自定义消息成功() {
        CrossServerProducer producer = mock(CrossServerProducer.class);
        CrossServerController controller = new CrossServerController(producer);

        CrossServerMessage message = CrossServerMessage.create("CUSTOM_TYPE", "src", null, "custom-payload");
        Map<String, Object> result = controller.sendMessage(message);

        assertTrue((Boolean) result.get("success"));
        assertEquals("跨服消息已发送", result.get("message"));
        verify(producer).sendMessage(message);
    }

    @Test
    void sendRechargeEvent_参数正确时返回成功() {
        CrossServerProducer producer = mock(CrossServerProducer.class);
        CrossServerController controller = new CrossServerController(producer);

        Map<String, Object> request = new HashMap<>();
        request.put("playerId", "1001");
        request.put("orderId", "ORD001");
        request.put("amount", "6");
        request.put("rechargeType", "monthly_card");

        Map<String, Object> result = controller.sendRechargeEvent(request);

        assertTrue((Boolean) result.get("success"));
        assertEquals("充值事件已发送", result.get("message"));
        verify(producer).sendRechargeEvent(1001L, "ORD001", 6.0, "monthly_card");
    }

    @Test
    void sendRechargeEvent_金额为小数() {
        CrossServerProducer producer = mock(CrossServerProducer.class);
        CrossServerController controller = new CrossServerController(producer);

        Map<String, Object> request = new HashMap<>();
        request.put("playerId", "3003");
        request.put("orderId", "ORD003");
        request.put("amount", "68.88");
        request.put("rechargeType", "direct_charge");

        controller.sendRechargeEvent(request);

        verify(producer).sendRechargeEvent(3003L, "ORD003", 68.88, "direct_charge");
    }

    @Test
    void health_返回服务状态() {
        CrossServerProducer producer = mock(CrossServerProducer.class);
        CrossServerController controller = new CrossServerController(producer);

        Map<String, Object> result = controller.health();

        assertEquals("UP", result.get("status"));
        assertEquals("game-service-a", result.get("service"));
    }
}
