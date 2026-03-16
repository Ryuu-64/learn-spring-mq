package top.ryuu64.learn.spring.springmq.learnspringmq.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 玩家升级事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLevelUpEvent {

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 玩家名称
     */
    private String playerName;

    /**
     * 旧等级
     */
    private Integer oldLevel;

    /**
     * 新等级
     */
    private Integer newLevel;

    /**
     * 升级时间
     */
    private Long timestamp;
}
