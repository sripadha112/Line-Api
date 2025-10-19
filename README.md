# appointment-auth-service

MVP Spring Boot service for OTP-based mobile login and registration (Doctor / User).

## Quick start

1. Update `src/main/resources/application.properties` with your Supabase credentials and a strong JWT secret.
2. Build and run:
   ```
   mvn clean package
   java -jar target/appointment-auth-service-0.0.1-SNAPSHOT.jar
   ```
3. Endpoints:
   - POST /api/auth/request-otp
   - POST /api/auth/verify-otp

Note: OTP is printed to console in this MVP. Integrate an SMS provider for production.
