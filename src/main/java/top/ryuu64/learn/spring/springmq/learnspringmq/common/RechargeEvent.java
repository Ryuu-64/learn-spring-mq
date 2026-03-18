package top.ryuu64.learn.spring.springmq.learnspringmq.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 玩家充值事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeEvent {

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 充值金额（元）
     */
    private Double amount;

    /**
     * 充值类型（如 "月卡", "直充", "礼包"）
     */
    private String rechargeType;

    /**
     * 充值时间戳
     */
    private Long timestamp;
}
