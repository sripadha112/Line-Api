# Firebase Environment Setup Script (PowerShell)

# This script helps you set up Firebase credentials securely using environment variables
# Choose one of the options below based on your preference

Write-Host "🔥 Firebase FCM Environment Setup" -ForegroundColor Yellow
Write-Host "=================================" -ForegroundColor Yellow

# Option 1: Set environment variable with file path
Write-Host "`n📁 Option 1: Using File Path" -ForegroundColor Cyan
Write-Host "Place your firebase-service-account-key.json outside the project directory"
Write-Host "Example commands:"
Write-Host '$env:GOOGLE_APPLICATION_CREDENTIALS="C:\firebase-keys\firebase-service-account-key.json"' -ForegroundColor Green

# Option 2: Set environment variable with base64 encoded credentials
Write-Host "`n🔐 Option 2: Using Base64 Encoding" -ForegroundColor Cyan
Write-Host "Encode your JSON file as base64 and set as environment variable"
Write-Host "Example commands:"
Write-Host '# First, encode the file (run this in PowerShell):' -ForegroundColor Gray
Write-Host '$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\firebase-service-account-key.json"))' -ForegroundColor Green
Write-Host '$env:FIREBASE_CREDENTIALS_BASE64=$base64' -ForegroundColor Green

# Option 3: Classpath resource (least secure for production)
Write-Host "`n📦 Option 3: Classpath Resource (Development Only)" -ForegroundColor Cyan
Write-Host "Replace the placeholder file in src/main/resources/"
Write-Host "⚠️  WARNING: Not recommended for production!" -ForegroundColor Red

Write-Host "`n🚀 Test Your Setup:" -ForegroundColor Yellow
Write-Host "After setting environment variables, test with:" -ForegroundColor Gray
Write-Host "mvn spring-boot:run" -ForegroundColor Green
Write-Host "curl http://localhost:8080/api/test/firebase-status" -ForegroundColor Green

Write-Host "`n📋 Current Environment Variables:" -ForegroundColor Yellow
if ($env:GOOGLE_APPLICATION_CREDENTIALS) {
    Write-Host "GOOGLE_APPLICATION_CREDENTIALS: $env:GOOGLE_APPLICATION_CREDENTIALS" -ForegroundColor Green
} else {
    Write-Host "GOOGLE_APPLICATION_CREDENTIALS: Not set" -ForegroundColor Red
}

if ($env:FIREBASE_CREDENTIALS_BASE64) {
    $preview = $env:FIREBASE_CREDENTIALS_BASE64.Substring(0, [Math]::Min(50, $env:FIREBASE_CREDENTIALS_BASE64.Length))
    Write-Host "FIREBASE_CREDENTIALS_BASE64: $preview..." -ForegroundColor Green
} else {
    Write-Host "FIREBASE_CREDENTIALS_BASE64: Not set" -ForegroundColor Red
}

Write-Host "`n✅ Recommendation for Production:" -ForegroundColor Yellow
Write-Host "Use Option 1 (file path) or Option 2 (base64) for production deployments" -ForegroundColor Gray
Write-Host "Never commit credential files to version control!" -ForegroundColor Red