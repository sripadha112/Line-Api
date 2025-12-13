# Pre-Push Security Check Script
# Run this before pushing to GitHub to ensure no secrets are exposed
# Usage: .\pre-push-check.ps1

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "GitHub Security Pre-Push Check" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$issues = @()
$warnings = @()

# Check staged files
Write-Host "Checking staged files..." -ForegroundColor Yellow

$stagedFiles = git diff --cached --name-only 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Not a git repository or git not installed" -ForegroundColor Red
    exit 1
}

# Dangerous file patterns
$dangerousPatterns = @(
    '\.json$',
    'base64\.txt$',
    '^\.env$',
    'firebase-credentials',
    'firebase-service-account',
    'google-services\.json',
    'GoogleService-Info\.plist'
)

foreach ($file in $stagedFiles) {
    foreach ($pattern in $dangerousPatterns) {
        if ($file -match $pattern) {
            $issues += "CRITICAL: Attempting to commit sensitive file: $file"
        }
    }
}

# Check for hardcoded secrets in code
Write-Host "Scanning for hardcoded secrets in staged files..." -ForegroundColor Yellow

$secretPatterns = @(
    @{Pattern = 'AIza[0-9A-Za-z-_]{35}'; Name = 'Google API Key'},
    @{Pattern = '"private_key":\s*"'; Name = 'Private Key in JSON'},
    @{Pattern = 'firebase\.initializeApp\(\{[^}]*apiKey'; Name = 'Firebase Config with API Key'},
    @{Pattern = 'mongodb(\+srv)?://[^:]+:[^@]+@'; Name = 'MongoDB Connection String'},
    @{Pattern = 'postgres://[^:]+:[^@]+@'; Name = 'PostgreSQL Connection String'},
    @{Pattern = 'password\s*=\s*"[^"]{8,}"'; Name = 'Hardcoded Password'},
    @{Pattern = 'secret\s*=\s*"[^"]{16,}"'; Name = 'Hardcoded Secret'}
)

foreach ($file in $stagedFiles) {
    if ($file -match '\.(java|properties|xml|yml|yaml)$') {
        $content = git diff --cached $file 2>&1
        
        foreach ($secretPattern in $secretPatterns) {
            if ($content -match $secretPattern.Pattern) {
                $warnings += "WARNING: Possible $($secretPattern.Name) found in: $file"
            }
        }
    }
}

# Check .gitignore exists and is comprehensive
if (-Not (Test-Path ".gitignore")) {
    $issues += "CRITICAL: .gitignore file not found!"
} else {
    $gitignoreContent = Get-Content ".gitignore" -Raw
    
    $requiredPatterns = @(
        'firebase-service-account',
        'line-application-.*-firebase',
        'base64\.txt',
        '\.env$'
    )
    
    foreach ($pattern in $requiredPatterns) {
        if (-Not ($gitignoreContent -match $pattern)) {
            $warnings += "WARNING: .gitignore missing pattern: $pattern"
        }
    }
}

# Check if Firebase JSON files exist in working directory
$firebaseFiles = Get-ChildItem -Path "src\main\resources" -Filter "*.json" -ErrorAction SilentlyContinue

foreach ($file in $firebaseFiles) {
    if ($file.Name -match "firebase|line-application|google-services") {
        Write-Host "  Found Firebase file (should be gitignored): $($file.FullName)" -ForegroundColor Gray
    }
}

# Display results
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Security Check Results" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

if ($issues.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host "No security issues detected" -ForegroundColor Green
    Write-Host "Safe to push to GitHub" -ForegroundColor Green
    Write-Host ""
    exit 0
}

if ($issues.Count -gt 0) {
    Write-Host "CRITICAL ISSUES FOUND:" -ForegroundColor Red
    Write-Host "======================" -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host "  X $issue" -ForegroundColor Red
    }
    Write-Host ""
}

if ($warnings.Count -gt 0) {
    Write-Host "WARNINGS:" -ForegroundColor Yellow
    Write-Host "=========" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "  ! $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}

if ($issues.Count -gt 0) {
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "DO NOT PUSH TO GITHUB!" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Actions required:" -ForegroundColor Yellow
    Write-Host "1. Remove sensitive files from staging:" -ForegroundColor White
    Write-Host "   git reset HEAD filename" -ForegroundColor Cyan
    Write-Host "2. Update .gitignore if needed" -ForegroundColor White
    Write-Host "3. Run this check again" -ForegroundColor White
    Write-Host ""
    exit 1
}

if ($warnings.Count -gt 0) {
    Write-Host "Please review the warnings above." -ForegroundColor Yellow
    Write-Host "If you are sure these are not security issues, you can proceed." -ForegroundColor Yellow
    Write-Host ""
    
    $response = Read-Host "Do you want to proceed with push? (yes/no)"
    if ($response -ne "yes") {
        Write-Host "Push cancelled." -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "Security check complete." -ForegroundColor Green
exit 0
