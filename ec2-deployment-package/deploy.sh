#!/bin/bash

###############################################################################
# EC2 Deployment Script
# Run this script to deploy/redeploy the application
###############################################################################

set -e  # Exit on error

APP_NAME="lineapp"
APP_DIR="/opt/lineapp"
JAR_NAME="line-application-1.0.0.jar"
SERVICE_NAME="lineapp"

echo "====================================="
echo "Deploying Line Application"
echo "====================================="

# Check if JAR exists in current directory
if [ ! -f "$JAR_NAME" ]; then
    echo "ERROR: $JAR_NAME not found in current directory!"
    echo "Please copy the JAR file here first."
    exit 1
fi

# Check if environment file exists
if [ ! -f ".env" ]; then
    echo "WARNING: .env file not found!"
    echo "Make sure environment variables are set in systemd service or create .env file"
fi

# Stop the service if running
echo "1. Stopping existing service..."
if systemctl is-active --quiet $SERVICE_NAME; then
    sudo systemctl stop $SERVICE_NAME
    echo "Service stopped"
else
    echo "Service not running"
fi

# Backup old JAR if it exists
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    echo "2. Backing up old JAR..."
    sudo mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)"
fi

# Copy new JAR
echo "3. Copying new JAR to $APP_DIR..."
sudo cp "$JAR_NAME" "$APP_DIR/"
sudo chown lineapp:lineapp "$APP_DIR/$JAR_NAME"

# Copy environment file if exists
if [ -f ".env" ]; then
    echo "4. Copying environment file..."
    sudo cp .env "$APP_DIR/.env"
    sudo chown lineapp:lineapp "$APP_DIR/.env"
    sudo chmod 600 "$APP_DIR/.env"
fi

# Copy Firebase credentials if exists
if [ -f "firebase-credentials.json" ]; then
    echo "5. Copying Firebase credentials..."
    sudo mkdir -p "$APP_DIR/config"
    sudo cp firebase-credentials.json "$APP_DIR/config/"
    sudo chown lineapp:lineapp "$APP_DIR/config/firebase-credentials.json"
    sudo chmod 600 "$APP_DIR/config/firebase-credentials.json"
    echo "✓ Firebase credentials copied to $APP_DIR/config/firebase-credentials.json"
else
    echo "WARNING: firebase-credentials.json not found in deployment package!"
    echo "FCM push notifications will not work without Firebase credentials."
fi

# Verify Firebase credentials exist in target location
if [ -f "$APP_DIR/config/firebase-credentials.json" ]; then
    echo "✓ Firebase credentials verified at $APP_DIR/config/firebase-credentials.json"
else
    echo "ERROR: Firebase credentials not found at $APP_DIR/config/firebase-credentials.json"
    echo "Please ensure firebase-credentials.json is in the deployment package."
fi

# Reload systemd and start service
echo "6. Starting service..."
sudo systemctl daemon-reload
sudo systemctl enable $SERVICE_NAME
sudo systemctl start $SERVICE_NAME

# Wait a moment for startup
echo "7. Waiting for application to start..."
sleep 5

# Check status
echo "8. Checking service status..."
sudo systemctl status $SERVICE_NAME --no-pager

# Check if application is responding
echo "9. Checking application health..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ Application is healthy!"
        break
    else
        echo "Waiting for application to respond... ($i/10)"
        sleep 3
    fi
done

echo ""
echo "====================================="
echo "Deployment Complete!"
echo "====================================="
echo "View logs: sudo journalctl -u $SERVICE_NAME -f"
echo "Service status: sudo systemctl status $SERVICE_NAME"
echo "Stop service: sudo systemctl stop $SERVICE_NAME"
echo "Restart service: sudo systemctl restart $SERVICE_NAME"
echo "====================================="
