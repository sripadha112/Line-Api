# # Multi-stage build for optimized image size
# FROM maven:3.9-eclipse-temurin-17 AS build

# # Set working directory
# WORKDIR /app

# # Copy pom.xml and download dependencies (cached layer)
# COPY pom.xml .
# RUN mvn dependency:go-offline -B

# # Copy source code
# COPY src ./src

# # Build the application (skip tests for faster builds)
# RUN mvn clean package -DskipTests -B

# # Runtime stage - use smaller JRE image
# FROM eclipse-temurin:17-jre-alpine

# # Add metadata
# LABEL maintainer="your-email@example.com"
# LABEL description="Line Backend Application with Firebase Cloud Messaging"

# # Create non-root user for security
# RUN addgroup -g 1001 -S appuser && \
#     adduser -u 1001 -S appuser -G appuser

# # Set working directory
# WORKDIR /app

# # Copy the built JAR from build stage
# COPY --from=build /app/target/*.jar app.jar

# # Change ownership to non-root user
# RUN chown -R appuser:appuser /app

# # Switch to non-root user
# USER appuser

# # Expose port (Render will use $PORT environment variable)
# EXPOSE 8080

# # Health check
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#     CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# # Run the application
# # Use shell form to allow environment variable expansion
# ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]



####### docker file for kedulz for ACA ######################

# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Runtime stage - use smaller JRE image
FROM eclipse-temurin:17-jre-alpine

# Add metadata
LABEL maintainer="your-email@example.com"
LABEL description="Kedulz Backend Application with Firebase Cloud Messaging"

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port (must match ACA Ingress target port)
EXPOSE 8080

# Health check (Docker-native; ACA uses its own probes configured separately in portal)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with JVM tuning for 0.5 vCPU / 1Gi container
# All app config (SPRING_PROFILES_ACTIVE, DB_*, FIREBASE_*, etc.) comes from
# ACA environment variables / secrets, NOT baked in here or read from a .env file
ENTRYPOINT ["sh", "-c", "java \
-Xms256m \
-Xmx640m \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:ParallelGCThreads=1 \
-XX:ConcGCThreads=1 \
-XX:InitiatingHeapOccupancyPercent=70 \
-XX:G1ReservePercent=10 \
-XX:+UseStringDeduplication \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:ReservedCodeCacheSize=48m \
-XX:MaxMetaspaceSize=128m \
-XX:MetaspaceSize=64m \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom \
-Dfile.encoding=UTF-8 \
-Djava.net.preferIPv4Stack=true \
-jar /app/app.jar"]