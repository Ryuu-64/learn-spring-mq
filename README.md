# learn-spring-mq

基于 Spring Boot + RocketMQ 的游戏后端消息队列示例项目。

## 技术栈

- Java 25
- Spring Boot 4.0.3
- RocketMQ 4.x 客户端 / 5.3.3 服务端
- Jackson JSON
- Lombok
- Gradle
- Docker Compose

## 项目结构

```
src/main/java/top/ryuu64/learn/spring/springmq/
├── common/                                  # 公共模型
│   ├── CrossServerMessage.java              # 跨服消息基类
│   ├── MessageType.java                    # 消息类型枚举
│   ├── PlayerLevelUpEvent.java              # 玩家升级事件
│   └── RechargeEvent.java                  # 充值事件
├── config/
│   └── RocketMQConfig.java                 # RocketMQ 配置
├── producer/
│   └── CrossServerProducer.java            # 消息生产者
├── consumer/
│   └── CrossServerConsumer.java            # 跨服消息消费者
├── recharge/                               # 充值场景（多消费者）
│   ├── DeliveryConsumer.java               # 发货消费者
│   ├── LogConsumer.java                   # 日志消费者
│   └── StatisticsConsumer.java             # 统计消费者
├── controller/
│   └── CrossServerController.java          # REST 测试接口
├── delivery/                               # 发货服务入口
│   └── DeliveryServiceApplication.java
├── logservice/                             # 日志服务入口
│   └── LogServiceApplication.java
├── statistics/                              # 统计服务入口
│   └── StatisticsServiceApplication.java
└── LearnSpringMqApplication.java            # 默认入口（跨服消息）
```

## 功能说明

### 场景一：跨服消息

通过 RocketMQ 实现游戏服务之间的跨服事件通信。

**消息流转：**

1. 通过 REST API 调用 `CrossServerProducer` 发送消息
2. 消息序列化为 JSON，发送到 RocketMQ 的 `cross-server-events` Topic
3. `CrossServerConsumer` 订阅该 Topic，按 `messageType` 分发处理

**CrossServerMessage 模型：**

| 字段            | 说明              |
|---------------|-----------------|
| messageId     | 消息唯一标识（UUID）    |
| messageType   | 消息类型            |
| sourceService | 发送方服务名称         |
| targetService | 目标服务名称          |
| timestamp     | 发送时间（ISO 格式字符串） |
| payload       | 消息内容（JSON）      |

**PlayerLevelUpEvent 模型：**

| 字段         | 说明    |
|------------|-------|
| playerId   | 玩家ID  |
| playerName | 玩家名称  |
| oldLevel   | 原等级   |
| newLevel   | 新等级   |
| timestamp  | 升级时间戳 |

**消息类型：**

| 类型                   | 说明    |
|----------------------|-------|
| PLAYER_LEVEL_UP      | 玩家升级  |
| ITEM_GAIN            | 获得物品  |
| ACHIEVEMENT_COMPLETE | 成就完成  |
| PLAYER_LOGIN         | 玩家登录  |
| PLAYER_LOGOUT        | 玩家登出  |
| LEADERBOARD_UPDATE   | 排行榜更新 |

---

### 场景二：充值事件（多消费者）

玩家充值成功后，一条消息同时被三个独立服务消费，每个服务处理自己的业务，互不干扰。

**消息流转：**

```
玩家充值成功
    │
    ▼
┌─────────────────────────────────────────┐
│              RocketMQ                    │
│         Topic: recharge-events           │
│            Tag: RECHARGE_SUCCESS        │
└─────────────────────────────────────────┘
    │                │                │
    ▼                ▼                ▼
┌────────┐    ┌────────┐    ┌────────────┐
│发货服务   │    │日志服务   │    │统计服务      │
│(delivery)│    │(log)    │    │(statistics)│
│ 发放钻石   │    │记录日志   │    │汇总数据      │
└────────┘    └────────┘    └────────────┘
端口 8081      端口 8082      端口 8083
```

**RechargeEvent 模型：**

| 字段          | 说明           |
|-------------|--------------|
| playerId    | 玩家ID         |
| orderId     | 订单号          |
| amount      | 充值金额（元）     |
| rechargeType | 充值类型（月卡/直充等） |
| timestamp   | 充值时间戳       |

## REST API

| 方法   | 路径                                | 参数                                       | 说明        |
|------|-----------------------------------|------------------------------------------|-----------|
| POST | `/api/cross-server/level-up`      | JSON body                                | 发送玩家升级事件  |
| POST | `/api/cross-server/send`          | JSON body                                | 发送自定义跨服消息 |
| POST | `/api/cross-server/send-with-tag` | `messageType`, `tag`, `payload` (URL 参数) | 发送带标签的消息  |
| POST | `/api/cross-server/recharge`      | JSON body                                | 发送充值成功事件  |
| GET  | `/api/cross-server/health`        | 无                                        | 健康检查      |

## 使用方式

### 1. 启动 RocketMQ

```shell
docker-compose up -d
```

该命令会启动三个容器：NameServer、Broker、Proxy。

### 2. 启动服务

**方式一：启动全部服务（单进程，充值三个消费者都在）**

```shell
./gradlew bootRun
```

应用运行在 `localhost:8081`。

**方式二：分别启动三个独立服务（真正分布式部署）**

```shell
# 终端 1 - 发货服务
./gradlew bootRun -Pargs=--spring.profiles.active=delivery
# 端口 8081

# 终端 2 - 日志服务
./gradlew bootRun -Pargs=--spring.profiles.active=log
# 端口 8082

# 终端 3 - 统计服务
./gradlew bootRun -Pargs=--spring.profiles.active=statistics
# 端口 8083
```

### 3. 测试

**测试跨服消息：**

```shell
curl -X POST http://localhost:8081/api/cross-server/level-up \
  -H "Content-Type: application/json" \
  -d '{"playerId": 1001, "playerName": "张三", "oldLevel": 10, "newLevel": 11}'
```

**测试充值事件：**

```shell
curl -X POST http://localhost:8081/api/cross-server/recharge \
  -H "Content-Type: application/json" \
  -d '{"playerId": 1001, "orderId": "ORD20260318001", "amount": 6, "rechargeType": "月卡"}'
```

也可以使用 `devops/misc.http` 文件在 IDEA 中直接发送请求。

## 备注

- RocketMQ 5.x 服务端兼容 4.x 客户端，可以正常通信
- 多消费者通过不同的 Consumer Group 实现，同一条消息会被每个 Group 独立消费一次
- Producer 和 Consumer 完全解耦，拆分部署时不需要修改任何业务代码

## 测试

```shell
./gradlew test
```

单元测试覆盖：

| 测试类 | 覆盖内容 |
|---|---|
| `CrossServerMessageTest` | 消息创建、字段赋值、UUID 唯一性 |
| `PlayerLevelUpEventTest` | 事件构造、字段访问 |
| `RechargeEventTest` | 事件构造、JSON 序列化/反序列化 |
| `CrossServerProducerTest` | Producer 发送消息、Topic/Tag 正确性 |
| `CrossServerControllerTest` | REST 接口参数解析、返回值 |
| `RechargeConsumerTest` | 业务逻辑（发货计算、统计聚合） |
- 多消费者通过不同的 Consumer Group 实现，同一条消息会被每个 Group 独立消费一次
- Producer 和 Consumer 完全解耦，拆分部署时不需要修改任何业务代码
