#!/bin/bash
# 启动日志服务
cd "$(dirname "$0")/../.."
./gradlew bootRun -Pargs=--spring.profiles.active=log
