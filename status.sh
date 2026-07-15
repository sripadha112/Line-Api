#!/bin/bash

###############################################################################
# Check Line Application Status
###############################################################################

APP_NAME="lineapp"
APP_DIR="/opt/lineapp"
PID_FILE="$APP_DIR/app.pid"

echo "=========================================="
echo "Line Application Status"
echo "=========================================="

# Check PID file
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    echo "PID file exists: $PID_FILE"
    echo "Recorded PID: $PID"
    
    # Check if process is running
    if ps -p $PID > /dev/null 2>&1; then
        echo "Status: ✓ RUNNING"
        echo ""
        
        # Show process info
        echo "Process Information:"
        ps -p $PID -o pid,user,%cpu,%mem,vsz,rss,start,time,comm
        echo ""
        
        # Check health endpoint
        echo "Health Check:"
        HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
        if [ $? -eq 0 ]; then
            echo "$HEALTH"
        else
            echo "⚠ WARNING: Health endpoint not responding"
        fi
        echo ""
        
        # Show memory usage
        echo "Memory Usage:"
        free -h
        echo ""
        
        # Show recent log entries
        echo "Recent Log Entries (last 5 lines):"
        if [ -f "/var/log/lineapp/application.log" ]; then
            tail -5 /var/log/lineapp/application.log
        else
            echo "Log file not found"
        fi
        
    else
        echo "Status: ✗ NOT RUNNING (stale PID file)"
        echo "PID $PID is not running"
    fi
else
    echo "PID file not found: $PID_FILE"
    
    # Try to find process
    PID=$(pgrep -f "line-application-1.0.0.jar")
    if [ -n "$PID" ]; then
        echo "Status: ⚠ RUNNING (but no PID file)"
        echo "Found process: $PID"
        echo ""
        ps -p $PID -o pid,user,%cpu,%mem,vsz,rss,start,time,comm
    else
        echo "Status: ✗ NOT RUNNING"
    fi
fi

echo ""
echo "=========================================="
