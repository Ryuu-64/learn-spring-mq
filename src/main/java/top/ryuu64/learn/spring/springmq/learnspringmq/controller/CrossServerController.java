package top.ryuu64.learn.spring.springmq.learnspringmq.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.ryuu64.learn.spring.springmq.learnspringmq.common.CrossServerMessage;
import top.ryuu64.learn.spring.springmq.learnspringmq.producer.CrossServerProducer;

import java.util.HashMap;
import java.util.Map;

/**
 * 跨服通信测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/cross-server")
@RequiredArgsConstructor
public class CrossServerController {

    private final CrossServerProducer crossServerProducer;

    /**
     * 发送玩家升级事件
     * POST /api/cross-server/level-up
     */
    @PostMapping("/level-up")
    public Map<String, Object> sendLevelUpEvent(@RequestBody Map<String, Object> request) {
        Long playerId = Long.valueOf(request.get("playerId").toString());
        String playerName = request.get("playerName").toString();
        Integer oldLevel = Integer.valueOf(request.get("oldLevel").toString());
        Integer newLevel = Integer.valueOf(request.get("newLevel").toString());

        crossServerProducer.sendPlayerLevelUpEvent(playerId, playerName, oldLevel, newLevel);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "玩家升级事件已发送");
        return result;
    }

    /**
     * 发送自定义跨服消息
     * POST /api/cross-server/send
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody CrossServerMessage message) {
        crossServerProducer.sendMessage(message);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "跨服消息已发送");
        return result;
    }

    /**
     * 发送带标签的消息
     * POST /api/cross-server/send-with-tag
     */
    @PostMapping("/send-with-tag")
    public Map<String, Object> sendMessageWithTag(
            @RequestParam String messageType,
            @RequestParam String tag,
            @RequestParam String payload) {

        CrossServerMessage message = CrossServerMessage.create(
                messageType,
                "game-service-a",
                null,
                payload
        );

        crossServerProducer.sendMessageWithTag(message, tag);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "带标签的消息已发送");
        return result;
    }

    /**
     * 发送充值成功事件
     * POST /api/cross-server/recharge
     */
    @PostMapping("/recharge")
    public Map<String, Object> sendRechargeEvent(@RequestBody Map<String, Object> request) {
        Long playerId = Long.valueOf(request.get("playerId").toString());
        String orderId = request.get("orderId").toString();
        Double amount = Double.valueOf(request.get("amount").toString());
        String rechargeType = request.get("rechargeType").toString();

        crossServerProducer.sendRechargeEvent(playerId, orderId, amount, rechargeType);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "充值事件已发送");
        return result;
    }

    /**
     * 健康检查
     * GET /api/cross-server/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "game-service-a");
        return result;
    }
}
