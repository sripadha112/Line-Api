# appointment-auth-service

MVP Spring Boot service for OTP-based mobile login and registration (Doctor / User).

## Environment Setup

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

## Quick start

1. **Set up environment variables** using one of the methods above.
2. **Build and run:**
   ```bash
   mvn clean package
   java -jar target/appointment-auth-service-0.0.1-SNAPSHOT.jar
   ```
3. **Access the application:**
   - **Swagger UI:** http://localhost:8080/swagger-ui/index.html
   - **Use the configured admin credentials for Basic Authentication**

## API Endpoints

- POST /api/auth/request-otp
- POST /api/auth/verify-otp

## Important Notes

- **Security:** The auto-generated password is now replaced with your custom admin credentials
- **Environment Variables:** All sensitive data (database credentials, admin password, JWT secret) are now configurable via environment variables
- **OTP:** OTP is printed to console in this MVP. Integrate an SMS provider for production.
- **Default Values:** If environment variables are not set, the application will use default values from `application.properties`
