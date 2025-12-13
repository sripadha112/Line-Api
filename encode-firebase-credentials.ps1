# Script to encode Firebase credentials to Base64 for secure deployment
# Usage: .\encode-firebase-credentials.ps1

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Firebase Credentials Base64 Encoder" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Firebase JSON file path
$firebaseJsonPath = "src\main\resources\line-application-84af7-firebase-adminsdk-fbsvc-a8505ed76a.json"

# Check if file exists
if (-Not (Test-Path $firebaseJsonPath)) {
    Write-Host "ERROR: Firebase credentials file not found at: $firebaseJsonPath" -ForegroundColor Red
    Write-Host "Please ensure the file exists before running this script." -ForegroundColor Yellow
    exit 1
}

Write-Host "Found Firebase credentials file: $firebaseJsonPath" -ForegroundColor Green
Write-Host ""

try {
    # Read the JSON file
    $fileContent = Get-Content -Path $firebaseJsonPath -Raw
    
    # Validate JSON structure
    try {
        $json = $fileContent | ConvertFrom-Json
        Write-Host "Valid JSON structure detected" -ForegroundColor Green
    } catch {
        Write-Host "WARNING: JSON validation failed. Continuing anyway..." -ForegroundColor Yellow
    }
    
    # Convert to Base64
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($fileContent)
    $base64 = [System.Convert]::ToBase64String($bytes)
    
    # Save to file
    $outputFile = "firebase-credentials-base64.txt"
    $base64 | Out-File -FilePath $outputFile -Encoding ASCII -NoNewline
    
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "Successfully encoded Firebase credentials!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Output saved to: $outputFile" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "NEXT STEPS:" -ForegroundColor Yellow
    Write-Host "1. Open $outputFile" -ForegroundColor White
    Write-Host "2. Copy the entire Base64 string" -ForegroundColor White
    Write-Host "3. In Render Dashboard, add environment variable:" -ForegroundColor White
    Write-Host "   Key: FIREBASE_CREDENTIALS_BASE64" -ForegroundColor Cyan
    Write-Host "   Value: <paste the Base64 string>" -ForegroundColor Cyan
    Write-Host "   Mark as: Secret" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "SECURITY WARNINGS:" -ForegroundColor Red
    Write-Host "• DO NOT commit $outputFile to Git" -ForegroundColor Yellow
    Write-Host "• DO NOT share this Base64 string publicly" -ForegroundColor Yellow
    Write-Host "• DO NOT include it in your source code" -ForegroundColor Yellow
    Write-Host "• The file is already in .gitignore" -ForegroundColor Green
    Write-Host ""
    
    # Display first and last 50 characters for verification
    if ($base64.Length -gt 100) {
        $preview = $base64.Substring(0, 50) + "..." + $base64.Substring($base64.Length - 50)
        Write-Host "Preview: $preview" -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "Total length: $($base64.Length) characters" -ForegroundColor Gray
    Write-Host ""
    Write-Host "SUCCESS! You can now proceed to push to GitHub and deploy to Render." -ForegroundColor Green
    
} catch {
    Write-Host ""
    Write-Host "ERROR: Failed to encode Firebase credentials" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
