#!/bin/bash
PID_FILE="/opt/lineapp/app.pid"
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "Status: RUNNING (PID: $PID)"
        curl -s http://localhost:8080/actuator/health
    else
        echo "Status: NOT RUNNING"
    fi
else
    echo "Status: NOT RUNNING"
fi
