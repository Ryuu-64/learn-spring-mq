# learn-spring-mq

基于 Spring Boot + RocketMQ 的游戏跨服通信示例项目。

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
src/main/java/top/ryuu64/learn/spring/springmq/learnspringmq/
├── common/
│   ├── CrossServerMessage.java     # 跨服消息基类
│   ├── MessageType.java            # 消息类型枚举
│   └── PlayerLevelUpEvent.java     # 玩家升级事件
├── config/
│   └── RocketMQConfig.java         # RocketMQ 配置
├── producer/
│   └── CrossServerProducer.java    # 消息生产者
├── consumer/
│   └── CrossServerConsumer.java    # 消息消费者
├── controller/
│   └── CrossServerController.java  # REST 测试接口
└── LearnSpringMqApplication.java
```

## 功能说明

### 消息模型

`CrossServerMessage` 是所有跨服消息的基类，包含以下字段：

| 字段 | 说明 |
|---|---|
| messageId | 消息唯一标识（UUID） |
| messageType | 消息类型 |
| sourceService | 发送方服务名称 |
| targetService | 目标服务名称 |
| timestamp | 发送时间（ISO 格式字符串） |
| payload | 消息内容（JSON） |

### PlayerLevelUpEvent

玩家升级事件的具体数据模型：

| 字段 | 说明 |
|---|---|
| playerId | 玩家ID |
| playerName | 玩家名称 |
| oldLevel | 原等级 |
| newLevel | 新等级 |
| timestamp | 升级时间戳 |

### 支持的消息类型

| 类型 | 说明 |
|---|---|
| PLAYER_LEVEL_UP | 玩家升级 |
| ITEM_GAIN | 获得物品 |
| ACHIEVEMENT_COMPLETE | 成就完成 |
| PLAYER_LOGIN | 玩家登录 |
| PLAYER_LOGOUT | 玩家登出 |
| LEADERBOARD_UPDATE | 排行榜更新 |

### 消息流转

1. 通过 REST API 或代码调用 `CrossServerProducer` 发送消息
2. 消息序列化为 JSON，发送到 RocketMQ 的 `cross-server-events` Topic
3. `CrossServerConsumer` 订阅该 Topic，收到消息后按 `messageType` 分发处理

### REST API

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| POST | `/api/cross-server/level-up` | JSON body | 发送玩家升级事件 |
| POST | `/api/cross-server/send` | JSON body | 发送自定义跨服消息 |
| POST | `/api/cross-server/send-with-tag` | `messageType`, `tag`, `payload` (URL 参数) | 发送带标签的消息 |
| GET | `/api/cross-server/health` | 无 | 健康检查 |

## 使用方式

### 1. 启动 RocketMQ

```shell
docker-compose up -d
```

该命令会启动三个容器：NameServer、Broker、Proxy。

### 2. 启动应用

```shell
./gradlew bootRun
```

应用默认运行在 `localhost:8081`。

### 3. 测试

```shell
curl -X POST http://localhost:8081/api/cross-server/level-up \
  -H "Content-Type: application/json" \
  -d '{"playerId": 1001, "playerName": "张三", "oldLevel": 10, "newLevel": 11}'
```

也可以使用 `devops/misc.http` 文件在 IDEA 中直接发送请求。

## 备注

- RocketMQ 5.x 服务端兼容 4.x 客户端，可以正常通信
- Producer 和 Consumer 解耦部署：拆分成两个独立服务连接同一个 RocketMQ 即可实现真正的跨服通信
