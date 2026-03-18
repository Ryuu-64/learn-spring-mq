#!/bin/bash
# 启动发货服务
cd "$(dirname "$0")/../.."
./gradlew bootRun -Pargs=--spring.profiles.active=delivery
