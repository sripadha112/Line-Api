#!/bin/bash

###############################################################################
# Quick FCM Fix Deployment Script
# Run this on EC2 to apply the FCM credentials fix
###############################################################################

set -e

APP_DIR="/opt/lineapp"
BACKUP_DIR="/opt/lineapp/backup-$(date +%Y%m%d_%H%M%S)"

echo "====================================="
echo "Applying FCM Push Notification Fix"
echo "====================================="

# Check if we're in the deployment directory
if [ ! -f "start.sh" ]; then
    echo "ERROR: start.sh not found in current directory!"
    echo "Please run this script from the deployment package directory"
    exit 1
fi

# Verify Firebase credentials exist
if [ ! -f "firebase-credentials.json" ]; then
    echo "ERROR: firebase-credentials.json not found!"
    echo "Please ensure Firebase credentials are in the deployment package"
    exit 1
fi

echo "1. Creating backup..."
sudo mkdir -p "$BACKUP_DIR"
if [ -f "$APP_DIR/start.sh" ]; then
    sudo cp "$APP_DIR/start.sh" "$BACKUP_DIR/"
fi
if [ -f "/opt/lineapp/config/firebase-credentials.json" ]; then
    sudo cp "/opt/lineapp/config/firebase-credentials.json" "$BACKUP_DIR/"
fi
echo "✓ Backup created at $BACKUP_DIR"

echo ""
echo "2. Stopping application..."
if [ -f "$APP_DIR/stop.sh" ]; then
    cd "$APP_DIR"
    sudo ./stop.sh || echo "Application not running"
else
    echo "WARNING: stop.sh not found"
fi

echo ""
echo "3. Deploying Firebase credentials..."
sudo mkdir -p "$APP_DIR/config"
sudo cp firebase-credentials.json "$APP_DIR/config/"
sudo chown lineapp:lineapp "$APP_DIR/config/firebase-credentials.json"
sudo chmod 600 "$APP_DIR/config/firebase-credentials.json"
echo "✓ Firebase credentials deployed"

echo ""
echo "4. Verifying Firebase credentials..."
if [ -f "$APP_DIR/config/firebase-credentials.json" ]; then
    echo "✓ Credentials file exists: $APP_DIR/config/firebase-credentials.json"
    FILE_SIZE=$(stat -f%z "$APP_DIR/config/firebase-credentials.json" 2>/dev/null || stat -c%s "$APP_DIR/config/firebase-credentials.json" 2>/dev/null)
    echo "  File size: $FILE_SIZE bytes"
    OWNER=$(stat -c '%U:%G' "$APP_DIR/config/firebase-credentials.json" 2>/dev/null || stat -f '%Su:%Sg' "$APP_DIR/config/firebase-credentials.json" 2>/dev/null)
    echo "  Owner: $OWNER"
else
    echo "ERROR: Firebase credentials file not found after deployment!"
    exit 1
fi

echo ""
echo "5. Updating start.sh script..."
sudo cp start.sh "$APP_DIR/"
sudo chmod +x "$APP_DIR/start.sh"
sudo chown lineapp:lineapp "$APP_DIR/start.sh"
echo "✓ Updated start.sh with GOOGLE_APPLICATION_CREDENTIALS environment variable"

echo ""
echo "6. Starting application..."
cd "$APP_DIR"
sudo ./start.sh

echo ""
echo "7. Waiting for application to start..."
sleep 8

echo ""
echo "8. Checking application status..."
if [ -f "$APP_DIR/app.pid" ]; then
    PID=$(cat "$APP_DIR/app.pid")
    if ps -p $PID > /dev/null 2>&1; then
        echo "✓ Application is running (PID: $PID)"
    else
        echo "WARNING: Application process not found"
    fi
else
    echo "WARNING: PID file not found"
fi

echo ""
echo "9. Checking application health..."
for i in {1..5}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ Application is healthy!"
        break
    else
        echo "Waiting for health check... ($i/5)"
        sleep 3
    fi
done

echo ""
echo "10. Checking Firebase initialization..."
sleep 2
if grep -q "Firebase application initialized successfully" /opt/lineapp/logs/application.log 2>/dev/null; then
    echo "✓ Firebase initialized successfully!"
else
    echo "⚠ Firebase initialization status unclear - check logs"
fi

echo ""
echo "====================================="
echo "FCM Fix Deployment Complete!"
echo "====================================="
echo ""
echo "Next Steps:"
echo "1. Monitor logs:    tail -f /opt/lineapp/logs/application.log"
echo "2. Test FCM:        bash /opt/lineapp/test-push-notifications.sh"
echo "3. Check errors:    grep -i 'firebase\\|fcm' /opt/lineapp/logs/application.log"
echo ""
echo "If FCM still fails:"
echo "- Verify GOOGLE_APPLICATION_CREDENTIALS is set in startup log"
echo "- Check: cat /opt/lineapp/logs/startup.log | grep Firebase"
echo "- Restart: sudo ./restart.sh"
echo "====================================="

