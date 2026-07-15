#!/bin/bash

###############################################################################
# Restart Line Application
###############################################################################

APP_NAME="lineapp"
APP_DIR="/home/ec2-user/kedulz-api"

echo "=========================================="
echo "Restarting Line Application"
echo "=========================================="

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Stop application
echo ""
echo "Step 1: Stopping application..."
bash "$SCRIPT_DIR/stop.sh"
STOP_STATUS=$?

if [ $STOP_STATUS -ne 0 ]; then
    echo "WARNING: Stop script returned non-zero status"
fi

# Wait a moment
echo ""
echo "Waiting 2 seconds before restart..."
sleep 2

# Start application
echo ""
echo "Step 2: Starting application..."
bash "$SCRIPT_DIR/start.sh"
START_STATUS=$?

if [ $START_STATUS -eq 0 ]; then
    echo ""
    echo "✓ Restart completed successfully"
    echo "=========================================="
    exit 0
else
    echo ""
    echo "✗ ERROR: Failed to restart application"
    echo "Check logs at: /home/ec2-user/kedulz-api/logs/application.out"
    echo "=========================================="
    exit 1
fi
