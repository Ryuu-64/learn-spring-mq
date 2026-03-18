package top.ryuu64.learn.spring.springmq.learnspringmq.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerLevelUpEventTest {

    @Test
    void 全参构造_字段正确赋值() {
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(
                1001L, "张三", 10, 11, 1700000000L
        );

        assertEquals(1001L, event.getPlayerId());
        assertEquals("张三", event.getPlayerName());
        assertEquals(10, event.getOldLevel());
        assertEquals(11, event.getNewLevel());
        assertEquals(1700000000L, event.getTimestamp());
    }

    @Test
    void 无参构造_字段为null或零值() {
        PlayerLevelUpEvent event = new PlayerLevelUpEvent();

        assertNull(event.getPlayerId());
        assertNull(event.getPlayerName());
        assertNull(event.getOldLevel());
        assertNull(event.getNewLevel());
        assertNull(event.getTimestamp());
    }

    @Test
    void setter_正常赋值() {
        PlayerLevelUpEvent event = new PlayerLevelUpEvent();
        event.setPlayerId(2002L);
        event.setPlayerName("李四");
        event.setOldLevel(5);
        event.setNewLevel(6);
        event.setTimestamp(1800000000L);

        assertEquals(2002L, event.getPlayerId());
        assertEquals("李四", event.getPlayerName());
        assertEquals(5, event.getOldLevel());
        assertEquals(6, event.getNewLevel());
        assertEquals(1800000000L, event.getTimestamp());
    }

    @Test
    void 全参构造_所有字段可访问() {
        long ts = System.currentTimeMillis();
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(1L, "王五", 1, 2, ts);

        assertAll(
                () -> assertEquals(1L, event.getPlayerId()),
                () -> assertEquals("王五", event.getPlayerName()),
                () -> assertEquals(1, event.getOldLevel()),
                () -> assertEquals(2, event.getNewLevel()),
                () -> assertEquals(ts, event.getTimestamp())
        );
    }
}
