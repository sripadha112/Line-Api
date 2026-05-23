#!/bin/bash

###############################################################################
# Stop Line Application
###############################################################################

APP_NAME="lineapp"
APP_DIR="/home/ec2-user/NeextApp-api"
PID_FILE="$APP_DIR/app.pid"

echo "=========================================="
echo "Stopping Line Application"
echo "=========================================="

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    echo "PID file not found: $PID_FILE"
    echo "Application may not be running"
    
    # Try to find process anyway
    PID=$(pgrep -f "line-application-1.0.0.jar")
    if [ -n "$PID" ]; then
        echo "Found running process: $PID"
        echo "Attempting to stop..."
    else
        echo "No running application found"
        exit 0
    fi
else
    PID=$(cat "$PID_FILE")
    echo "Found PID: $PID"
fi

# Check if process is running
if ! ps -p $PID > /dev/null 2>&1; then
    echo "Process $PID is not running"
    rm -f "$PID_FILE"
    echo "Cleaned up PID file"
    exit 0
fi

# Graceful shutdown
echo "Sending SIGTERM to process $PID..."
kill -15 $PID

# Wait for graceful shutdown (max 30 seconds)
echo "Waiting for graceful shutdown..."
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "✓ Application stopped gracefully"
        rm -f "$PID_FILE"
        echo "=========================================="
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo ""
echo "Application did not stop gracefully"

# Force kill if still running
if ps -p $PID > /dev/null 2>&1; then
    echo "Forcing shutdown with SIGKILL..."
    sudo kill -9 $PID
    sleep 2
    
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "✓ Application force-stopped"
        rm -f "$PID_FILE"
    else
        echo "✗ ERROR: Could not stop application"
        exit 1
    fi
fi

echo "=========================================="
