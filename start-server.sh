#!/bin/bash
# 启动 ms-server-java（v3 分支，支持 FLAC 无损播放）
# 密钥从 COS_SECRET_ID / COS_SECRET_KEY 环境变量读取，如果未设置则从 .bashrc 获取

if [ -z "$COS_SECRET_ID" ] || [ -z "$COS_SECRET_KEY" ]; then
  # 尝试从 .bashrc 读取（跳过 [ -z "$PS1" ] && return 保护）
  source <(grep 'export COS_SECRET' /root/.bashrc)
fi

if [ -z "$COS_SECRET_ID" ]; then
  echo "ERROR: COS_SECRET_ID not set"
  exit 1
fi

JAR_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$JAR_DIR/ms-server-java/target/ms-server-1.0.0.jar"

if [ ! -f "$JAR" ]; then
  echo "ERROR: JAR not found at $JAR"
  exit 1
fi

echo "Starting ms-server with COS auth..."
COS_SECRET_ID="$COS_SECRET_ID" COS_SECRET_KEY="$COS_SECRET_KEY" \
  nohup java -jar "$JAR" > /tmp/ms-server.log 2>&1 &

echo "PID: $!"
sleep 2
tail -3 /tmp/ms-server.log
