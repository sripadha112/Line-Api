# Line Healthcare Application

Spring Boot service for healthcare appointment management with OTP-based authentication, push notifications, and comprehensive booking management.

## 🚀 Quick Links

- **Local Development**: See sections below
- **AWS EC2 Deployment**: [EC2_QUICK_START.md](EC2_QUICK_START.md) - Deploy to production in 30 minutes
- **Complete EC2 Guide**: [AWS_EC2_DEPLOYMENT_GUIDE.md](AWS_EC2_DEPLOYMENT_GUIDE.md)
- **Deployment Summary**: [EC2_DEPLOYMENT_SUMMARY.md](EC2_DEPLOYMENT_SUMMARY.md)

## Local Development Setup

### Option 1: Using Setup Scripts (Recommended)

**For PowerShell:**
```powershell
.\setup-env.ps1
```

**For Command Prompt:**
```cmd
setup-env.bat
```

### Option 2: Manual Environment Variables Setup

Set the following environment variables before running the application:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://your-db-host:5432/your-db
DATABASE_USERNAME=your-db-username
DATABASE_PASSWORD=your-db-password

# Admin User for Swagger/API Access
ADMIN_USERNAME=admin
ADMIN_PASSWORD=yourCustomPassword123
ADMIN_ROLES=ADMIN

# JWT Configuration
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeLongAndSecure2024!
JWT_EXPIRATION_MS=2592000000

# Server Configuration (Optional)
SERVER_PORT=8080
```

### Option 3: Using IDE Environment Variables

If using IntelliJ IDEA or Eclipse, you can set environment variables in your run configuration.

## Quick Start (Local)

1. **Set up environment variables** using one of the methods above.
2. **Build and run:**
   ```bash
   mvn clean package
   java -jar target/line-application-1.0.0.jar
   ```
3. **Access the application:**
   - **Swagger UI:** http://localhost:8080/swagger-ui/index.html
   - **Health Check:** http://localhost:8080/actuator/health
   - **Use the configured admin credentials for Basic Authentication**

## 🌐 Production Deployment (AWS EC2)

Deploy to AWS EC2 free tier in ~30 minutes:

```powershell
# 1. Build deployment package
.\build-ec2-package.ps1

# 2. Configure .env with your credentials
# Edit ec2-deployment-package\.env

# 3. Follow deployment guide
# See EC2_QUICK_START.md
```

**📚 Documentation:**
- [EC2_QUICK_START.md](EC2_QUICK_START.md) - Quick 30-minute deployment
- [AWS_EC2_DEPLOYMENT_GUIDE.md](AWS_EC2_DEPLOYMENT_GUIDE.md) - Complete reference
- [EC2_DEPLOYMENT_SUMMARY.md](EC2_DEPLOYMENT_SUMMARY.md) - Overview of all files
- [NGINX_CONFIG.md](NGINX_CONFIG.md) - Nginx setup with SSL

**Features:**
- ✅ Production-optimized configuration for t2.micro
- ✅ Automated deployment scripts
- ✅ Systemd service with auto-restart
- ✅ Health monitoring via Actuator
- ✅ Nginx reverse proxy support
- ✅ $0/month (within AWS free tier)

## Technology Stack

- **Framework**: Spring Boot 3.3.2
- **Java**: 17
- **Database**: PostgreSQL (Supabase)
- **Security**: JWT, Spring Security
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **API Documentation**: Swagger/OpenAPI
- **Monitoring**: Spring Boot Actuator

## API Endpoints

- POST /api/auth/request-otp
- POST /api/auth/verify-otp

## Important Notes

- **Security:** The auto-generated password is now replaced with your custom admin credentials
- **Environment Variables:** All sensitive data (database credentials, admin password, JWT secret) are now configurable via environment variables
- **OTP:** OTP is printed to console in this MVP. Integrate an SMS provider for production.
- **Default Values:** If environment variables are not set, the application will use default values from `application.properties`
