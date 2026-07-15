#!/bin/bash

###############################################################################
# EC2 Application Monitoring Script
# Quick health check and monitoring for Line Application
###############################################################################

APP_NAME="lineapp"
APP_URL="http://localhost:8080"
LOG_FILE="/var/log/lineapp/monitor.log"

echo "========================================"
echo "Line Application Health Check"
echo "========================================"
echo "Time: $(date)"
echo ""

# Function to check service status
check_service() {
    echo "1. Service Status:"
    if systemctl is-active --quiet $APP_NAME; then
        echo "   ✓ Service is running"
        return 0
    else
        echo "   ✗ Service is NOT running"
        return 1
    fi
}

# Function to check application health endpoint
check_health() {
    echo ""
    echo "2. Application Health:"
    
    response=$(curl -s -w "\n%{http_code}" "$APP_URL/actuator/health" 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        echo "   ✓ Application is healthy (HTTP $http_code)"
        echo "   Response: $body"
        return 0
    else
        echo "   ✗ Application health check failed (HTTP $http_code)"
        echo "   Response: $body"
        return 1
    fi
}

# Function to check database connectivity
check_database() {
    echo ""
    echo "3. Database Connection:"
    
    db_health=$(curl -s "$APP_URL/actuator/health" 2>/dev/null | grep -o '"db":{"status":"[^"]*"' || echo "unknown")
    
    if echo "$db_health" | grep -q "UP"; then
        echo "   ✓ Database connection is healthy"
        return 0
    else
        echo "   ⚠ Database status: $db_health"
        return 1
    fi
}

# Function to check system resources
check_resources() {
    echo ""
    echo "4. System Resources:"
    
    # Memory usage
    mem_info=$(free -h | grep Mem)
    mem_used=$(echo $mem_info | awk '{print $3}')
    mem_total=$(echo $mem_info | awk '{print $2}')
    echo "   Memory: $mem_used / $mem_total used"
    
    # Disk usage
    disk_usage=$(df -h / | tail -n1 | awk '{print $5}')
    echo "   Disk: $disk_usage used"
    
    # CPU load
    load_avg=$(uptime | awk -F'load average:' '{print $2}')
    echo "   Load average:$load_avg"
    
    # Java process memory
    if pgrep -f "line-application" > /dev/null; then
        java_pid=$(pgrep -f "line-application")
        java_mem=$(ps -p $java_pid -o rss= | awk '{printf "%.0f MB\n", $1/1024}')
        echo "   Java process memory: $java_mem"
    fi
}

# Function to show recent errors from logs
check_errors() {
    echo ""
    echo "5. Recent Errors (last 10):"
    
    error_count=$(sudo journalctl -u $APP_NAME --since "1 hour ago" -p err | wc -l)
    
    if [ $error_count -gt 0 ]; then
        echo "   ⚠ Found $error_count errors in last hour"
        echo ""
        sudo journalctl -u $APP_NAME --since "1 hour ago" -p err | tail -n 10
    else
        echo "   ✓ No errors in last hour"
    fi
}

# Function to show application uptime
check_uptime() {
    echo ""
    echo "6. Application Uptime:"
    
    if systemctl is-active --quiet $APP_NAME; then
        uptime_info=$(systemctl show $APP_NAME --property=ActiveEnterTimestamp)
        echo "   $uptime_info"
    else
        echo "   Service is not running"
    fi
}

# Run all checks
check_service
service_status=$?

check_health
health_status=$?

check_database

check_resources

check_errors

check_uptime

# Summary
echo ""
echo "========================================"
echo "Health Check Summary"
echo "========================================"

if [ $service_status -eq 0 ] && [ $health_status -eq 0 ]; then
    echo "Status: ✓ ALL SYSTEMS OPERATIONAL"
    exit 0
else
    echo "Status: ✗ ISSUES DETECTED"
    echo ""
    echo "Troubleshooting commands:"
    echo "  View logs: sudo journalctl -u $APP_NAME -f"
    echo "  Restart:   sudo systemctl restart $APP_NAME"
    echo "  Status:    sudo systemctl status $APP_NAME"
    exit 1
fi
