package top.ryuu64.learn.spring.springmq.learnspringmq.common;

/**
 * 跨服消息类型枚举
 */
public enum MessageType {

    /**
     * 玩家升级事件
     */
    PLAYER_LEVEL_UP("玩家升级"),

    /**
     * 获得物品事件
     */
    ITEM_GAIN("获得物品"),

    /**
     * 成就完成事件
     */
    ACHIEVEMENT_COMPLETE("成就完成"),

    /**
     * 玩家登录事件
     */
    PLAYER_LOGIN("玩家登录"),

    /**
     * 玩家登出事件
     */
    PLAYER_LOGOUT("玩家登出"),

    /**
     * 排行榜更新事件
     */
    LEADERBOARD_UPDATE("排行榜更新");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
