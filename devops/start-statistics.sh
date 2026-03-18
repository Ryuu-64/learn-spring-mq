#!/bin/bash
# 启动统计服务
cd "$(dirname "$0")/../.."
./gradlew bootRun -Pargs=--spring.profiles.active=statistics
