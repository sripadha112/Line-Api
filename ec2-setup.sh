#!/bin/bash

###############################################################################
# EC2 Initial Setup Script
# Run this once on a fresh EC2 instance to set up the environment
###############################################################################

set -e  # Exit on error

echo "====================================="
echo "Starting EC2 Instance Setup"
echo "====================================="

# Update system packages
echo "1. Updating system packages..."
sudo yum update -y

# Install Java 17 (Amazon Corretto)
echo "2. Installing Java 17..."
sudo yum install -y java-17-amazon-corretto-devel
java -version

# Verify Java installation
if ! command -v java &> /dev/null; then
    echo "ERROR: Java installation failed!"
    exit 1
fi

# Create application directory
echo "3. Creating application directories..."
sudo mkdir -p /opt/lineapp
sudo mkdir -p /opt/lineapp/logs
sudo mkdir -p /opt/lineapp/config
sudo mkdir -p /var/log/lineapp
echo "✓ Application directories created:
   - /opt/lineapp (application files)
   - /opt/lineapp/config (configuration files)
   - /var/log/lineapp (log files - application.log, request.log, access_log)"

# Create application user (non-root for security)
echo "4. Creating application user..."
if ! id -u lineapp > /dev/null 2>&1; then
    sudo useradd -r -s /bin/false lineapp
    echo "User 'lineapp' created"
else
    echo "User 'lineapp' already exists"
fi

# Set permissions
echo "5. Setting directory permissions..."
sudo chown -R lineapp:lineapp /opt/lineapp
sudo chown -R lineapp:lineapp /var/log/lineapp
sudo chmod 755 /opt/lineapp
sudo chmod 755 /var/log/lineapp

# Install monitoring tools (optional but recommended)
echo "6. Installing monitoring tools..."
sudo yum install -y htop curl wget

# Configure firewall (if firewalld is running)
echo "7. Configuring firewall..."
if systemctl is-active --quiet firewalld; then
    sudo firewall-cmd --permanent --add-port=8080/tcp
    sudo firewall-cmd --reload
    echo "Firewall configured"
else
    echo "Firewalld not running - skipping firewall configuration"
fi

# Install nginx as reverse proxy (optional)
read -p "Do you want to install Nginx as reverse proxy? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "8. Installing Nginx..."
    sudo amazon-linux-extras install -y nginx1
    sudo systemctl enable nginx
    echo "Nginx installed. You'll need to configure it manually."
else
    echo "8. Skipping Nginx installation"
fi

echo ""
echo "====================================="
echo "EC2 Setup Complete!"
echo "====================================="
echo "Next steps:"
echo "1. Upload your application JAR to /opt/lineapp/"
echo "2. Create environment file at /opt/lineapp/.env"
echo "3. Upload Firebase credentials to /opt/lineapp/config/"
echo "4. Run deploy.sh to deploy the application"
echo "====================================="
