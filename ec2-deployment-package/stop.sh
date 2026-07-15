#!/bin/bash
PID_FILE="/opt/lineapp/app.pid"
echo "Stopping Application..."
if [ ! -f "$PID_FILE" ]; then echo "Not running"; exit 0; fi
PID=$(cat "$PID_FILE")
if ! ps -p $PID > /dev/null 2>&1; then echo "Not running"; rm -f "$PID_FILE"; exit 0; fi
kill -15 $PID
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then echo "Stopped"; rm -f "$PID_FILE"; exit 0; fi
    sleep 1
done
kill -9 $PID 2>/dev/null
rm -f "$PID_FILE"
echo "Stopped"
