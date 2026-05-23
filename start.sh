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
export FIREBASE_CREDENTIALS_BASE64="ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAibGluZS1hcHBsaWNhdGlvbi04NGFmNyIsCiAgInByaXZhdGVfa2V5X2lkIjogImE4NTA1ZWQ3NmE1MTA4ZWYxNDQzNDE2ZTNlNDU0ZTE1ZjYxNDMwNTYiLAogICJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRQ1BtTG9QRVpvcGhZU1VcbkNMUW9SOWg3bjAxOVhGMDVGWjczdVpNaHE3ZUxsYVZEUGNBVlR1c0YvRWsvamRMcnk1em5oNzZpemxvdWNEMkJcblRRRUdMSVluK2RSM1p4MWdza2NNeW9lTFI1TmVMa3pRTHQyaVRyUlF6aEwvNDFHeUlranVqamdobVNFck9CRDhcbnFsTUV1OExXdHduYVZqc201ZC9pbUNnczRHWVR6Y1FyRk9Fb2xPVXZZYk5Yb3hDWWZQN2xPZGE1UU8wRmN2dU1cbkdINWtzNkNtdW0yb0kxYjdOSGd4MkJpSXJSUWJ6N3lMeEN5TEI0Y0ljQU02cXluSXJCOG1YTzNEU2plb3BzYkxcbmpaY1U0RzhUenRQRkJiRDlYZk5xeHZSamhtdVpqb1dvdnBqR0dIWUl1NlNHcW5XeFJWSHNiL1Z6RVg5K015cFpcbnFQT0ZHOWN2QWdNQkFBRUNnZ0VBQVdiUlZoeUVrczZVN21pVWltYnA3WW91MUl6c3dHeGtnUEMzNFFuQ0p6Q0xcbmN1c1RTUUgxNVI5Y21OK0RhNlJrUUdLejZzT0I0QXBkMUIvRm1IeTdYdWE0NFg2dnE5TGJRdUthQUV5OERqWmlcbjRpZ2hXa3VHa3RvazVGVWd6SVFHOU5FbktodjkxY3FwYUpYMWJPUG5GNnB4aEJQRk13cE14dG0rWFF3czR1WDhcbk8yRWtCYjZoSGZkU3JReDFkd08ybStiejR3VXg2a2ErSnYwT0JFdkVDOVRwa29OWW5VVXFmSzBOZ0d0Mkhld3pcbjZEYUZXRDI4Y05LVG5zNE03ZEcyMXF3aTAxdnlLaHpMWHFGeTNXZVJ6OVh4K0RNZ0h3bXZDc01TeGNyTHJ0cDdcbkNaWVdvd3RUZk5kc0VvNEJ4RzJuNkFscU0rUWFYbGhuQ2RYVkhTTk9tUUtCZ1FERXdsNE1lWUFHdWpGclVQYmVcblVpSjhjcytKcU9XSnMrU1lTSEJwTVBRcm40K3JrVUJHcWxoVHRleUlzczk5RllyaVRFb1U5dFRqUVZ1eWhuKzZcbjJ2UzhZcW0rcDlIeERZZXY5N0ErM29zOTJ3MURkUDRNa0U1M2E1MVh1VWFwV1pJYzc2eDhqdDdQVVl0d2N1MWxcblFHSno1cFpadFpuVHR2WTYzbTl2bVVQVGlRS0JnUUM2MUx4YVJicytCajJYUEFzeUM1MWZHcXRnUG1MRytnMVFcbjE5ZmM5SnJ4Y29PTzVTMGdveEQ4QWlDT1B1Tzg5L2x4L2llMTNrZThwVllyUU8xa0EvMVF3dWpqMVgwSFVCcExcblNEdFp0aTNPcDBQYzJieUV5UzZCeC9oMGdBQi93bGQwbE94blBPSDFGZFFubWJyR08ybHZ1KzFJSTNrV2ZTekdcblB6VkxMNzlPOXdLQmdFTnVTVVVGTHJjZmdHaEM2eGZ1Rmh1SVRVL2tMaVl2SEp5RDF1SjJBSGdpeXQ0RmZYMjZcblMyTUU2ZmMvTUpTeG1WTk15ZWE5WU9BeE9mZkkyR1YxUElLRFhhRVZhb0d1SFY1VkNIWERWdnp2NFcvSTNIZjVcbnpLcG9teGZCUzFJbWZFaW1hdnFWREE4STV6eTJabDFZMzJUUkZaM1ErdEx2MVJyL3VEa0swWS9oQW9HQVBsODlcbkRrb1BTNUtieHp4MVpGeTZTZ0RHUzlnbjg5T1ExSGRxaWtwaTI4NW1Hek5wVUdjQUNaSFFPb3pHVE5UZ1F1MGRcblpycnhhZ29zYlQ0OHdsSU9wSUtkYTBwZmRpeUl5cTh2bVRDWHRGRGs5L3I4MDd1eU9nTi9iNjAzL1c3czhXK3lcblNIanIybjFTR1hKYUIwbllWa1AvNEVCdmNWR1N2QkJvQTFUS1lsc0NnWUVBbVUxQ01QRlpFY29HS0QyWk1ncllcbkFqRmRid1VSZUVHdFIrQWcwZnNOeWNNL013T0QrUHV0aWQvMXJxRXJOOXUydnBQYXgrdGZna3dJK1dYeFkzS2RcbjdYWEo3bS9NMDBXY3NWV1EzVEYvVHdVUHVEYzlPc1hNOG1FQWVlem5TdUVkTGhTMVhXbzBDNTI4eXFyZlFUYlBcbktYTWVMWGc3U1VFckMyMW9PM0h6ZVFjPVxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwKICAiY2xpZW50X2VtYWlsIjogImZpcmViYXNlLWFkbWluc2RrLWZic3ZjQGxpbmUtYXBwbGljYXRpb24tODRhZjcuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLAogICJjbGllbnRfaWQiOiAiMTA2OTA5MjM4OTczMjk3MTcyNDAwIiwKICAiYXV0aF91cmkiOiAiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tL28vb2F1dGgyL2F1dGgiLAogICJ0b2tlbl91cmkiOiAiaHR0cHM6Ly9vYXV0aDIuZ29vZ2xlYXBpcy5jb20vdG9rZW4iLAogICJhdXRoX3Byb3ZpZGVyX3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vb2F1dGgyL3YxL2NlcnRzIiwKICAiY2xpZW50X3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vcm9ib3QvdjEvbWV0YWRhdGEveDUwOS9maXJlYmFzZS1hZG1pbnNkay1mYnN2YyU0MGxpbmUtYXBwbGljYXRpb24tODRhZjcuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLAogICJ1bml2ZXJzZV9kb21haW4iOiAiZ29vZ2xlYXBpcy5jb20iCn0K"
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
