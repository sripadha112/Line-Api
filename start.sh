#!/bin/bash

###############################################################################
# Start Line Application
# Optimized JVM settings for Java 17 on EC2 t2.micro (1GB RAM)
###############################################################################

APP_NAME="lineapp"
APP_DIR="/home/ec2-user/NeextApp-api"
JAR_FILE="$APP_DIR/line-application-1.0.0.jar"
PID_FILE="$APP_DIR/app.pid"
LOG_DIR="/home/ec2-user/NeextApp-api/logs"

# JVM Options optimized for Java 17 and minimal RAM usage
JAVA_OPTS="-Xms256m \
-Xmx512m \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:ParallelGCThreads=1 \
-XX:ConcGCThreads=1 \
-XX:InitiatingHeapOccupancyPercent=70 \
-XX:G1ReservePercent=10 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:ReservedCodeCacheSize=64m \
-XX:InitialCodeCacheSize=32m \
-XX:MaxMetaspaceSize=128m \
-XX:MetaspaceSize=64m \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom \
-Dfile.encoding=UTF-8 \
-Djava.net.preferIPv4Stack=true"

# Spring Profile
SPRING_OPTS="--spring.profiles.active=prod"

echo "=========================================="
echo "Starting Line Application"
echo "=========================================="

# Check if already running
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "ERROR: Application is already running (PID: $PID)"
        echo "Use restart.sh to restart or stop.sh to stop first"
        exit 1
    else
        echo "Removing stale PID file"
        rm -f "$PID_FILE"
    fi
fi

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found: $JAR_FILE"
    exit 1
fi

# Create log directories if not exist
if [ ! -d "$LOG_DIR" ]; then
    echo "Creating log directory: $LOG_DIR"
    mkdir -p "$LOG_DIR"
fi

REQUEST_LOG_DIR="/home/ec2-user/NeextApp-api/request"
if [ ! -d "$REQUEST_LOG_DIR" ]; then
    echo "Creating request log directory: $REQUEST_LOG_DIR"
    mkdir -p "$REQUEST_LOG_DIR"
fi

# Source environment variables
if [ -f "$APP_DIR/.env" ]; then
    echo "Loading environment variables from .env"
    set -a
    source "$APP_DIR/.env"
    set +a
else
    echo "WARNING: .env file not found at $APP_DIR/.env"
fi

# Firebase credentials - Base64 encoded (for Expo push notifications)
echo "✓ Firebase credentials configured (Base64)"

# Start application
echo "Starting application..."
echo "JAR: $JAR_FILE"
echo "JVM Options: $JAVA_OPTS"
echo "Log Directory: $LOG_DIR"
echo ""

cd "$APP_DIR"

# Get current date for log file
LOG_DATE=$(date +%Y-%m-%d)

# Start application
nohup java $JAVA_OPTS -jar "$JAR_FILE" $SPRING_OPTS \
    > "$LOG_DIR/application-${LOG_DATE}.out" 2>&1 &

APP_PID=$!

# Save PID
echo $APP_PID > "$PID_FILE"

echo "Application started with PID: $APP_PID"
echo "PID saved to: $PID_FILE"
echo ""

# Wait a moment and check if still running
sleep 3

if ps -p $APP_PID > /dev/null 2>&1; then
    echo "✓ Application is running"
    echo ""
    echo "View logs:"
    echo "  tail -f $LOG_DIR/application.out"
    echo "  tail -f $LOG_DIR/application.log"
    echo "  tail -f $LOG_DIR/request.log"
    echo ""
    echo "Check health:"
    echo "  curl http://localhost:8080/actuator/health"
    echo ""
    echo "=========================================="
    exit 0
else
    echo "✗ ERROR: Application failed to start"
    echo "Check logs at: $LOG_DIR/application.out"
    rm -f "$PID_FILE"
    exit 1
fi
