#!/bin/bash
APP_DIR="/opt/lineapp"
JAR_FILE="$APP_DIR/line-application-1.0.0.jar"
PID_FILE="$APP_DIR/app.pid"

echo "Starting Application..."

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "ERROR: Already running (PID: $PID)"
        exit 1
    fi
    rm -f "$PID_FILE"
fi

mkdir -p /opt/lineapp/logs /opt/lineapp/request-logs

# Set Firebase credentials path for FCM push notifications
export GOOGLE_APPLICATION_CREDENTIALS="/home/ec2-user/kedulz-api/config/firebase-credentials.json"
echo "Firebase credentials: $GOOGLE_APPLICATION_CREDENTIALS"

cd "$APP_DIR"
nohup java -Xms256m -Xmx512m -XX:+UseG1GC -Dspring.profiles.active=prod -Dspring.config.additional-location=file:/opt/lineapp/application-prod.properties -jar "$JAR_FILE" > /opt/lineapp/logs/startup.log 2>&1 &

PID=$!
echo $PID > "$PID_FILE"
sleep 5

if ps -p $PID > /dev/null 2>&1; then
    echo "Started (PID: $PID)"
    echo "Logs: tail -f /opt/lineapp/logs/application.log"
else
    echo "Failed. Check: cat /opt/lineapp/logs/startup.log"
    rm -f "$PID_FILE"
    exit 1
fi