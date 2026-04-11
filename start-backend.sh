#!/bin/bash
# 农业大数据联合建模平台 - 启动脚本
# 使用方法: ./start-backend.sh

# 加载环境变量
if [ -f .env ]; then
    echo "加载环境变量..."
    export $(grep -v '^#' .env | xargs)
else
    echo "警告: .env 文件不存在，请复制 .env.example 并填写实际值"
    echo "使用默认配置启动..."
    
    # 开发环境默认值 (生产环境请删除这些默认值)
    export DB_USERNAME=${DB_USERNAME:-root}
    export DB_PASSWORD=${DB_PASSWORD:-admin123}
    export REDIS_PASSWORD=${REDIS_PASSWORD:-}
    export JWT_SECRET=${JWT_SECRET:-AgriPlatformSecretKey2026VeryLongSecureKey}
    export MAIL_USERNAME=${MAIL_USERNAME:-admpchina@yeah.net}
    export MAIL_PASSWORD=${MAIL_PASSWORD:-PHZt3NE4DtTTJSy8}
    export ZAI_API_KEY=${ZAI_API_KEY:-84b0c10a19954030994339f9101197f5.T8hZJQvRlisAOpmE}
fi

# 停止旧进程
echo "停止旧的后端服务..."
lsof -ti:8100 | xargs kill -9 2>/dev/null

# 等待端口释放
sleep 2

# 启动新服务
echo "启动后端服务..."
cd backend
nohup java -jar target/agri-platform-0.0.1-SNAPSHOT.jar > /tmp/backend.log 2>&1 &

echo "后端服务已启动，日志文件: /tmp/backend.log"
echo "检查服务状态..."
sleep 5
curl -s http://localhost:8100/api/hello || echo "服务启动中..."
