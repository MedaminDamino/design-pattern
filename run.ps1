# run.ps1 – Auto-configure and launch the JavaFX Drawing Application
# Usage: .\run.ps1

$ErrorActionPreference = "Stop"

Write-Host "==============================================" -ForegroundColor Green
Write-Host "🎨 JavaFX Drawing Application Bootstrapper" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
Write-Host ""

$projectDir = $PSScriptRoot

# ── Step 1: Verify Java Installation ───────────────────────────────────────
Write-Host "Checking Java installation..." -ForegroundColor Cyan
if (Get-Command java -ErrorAction SilentlyContinue) {
    $oldEAP = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $javaVersionStr = & java -version 2>&1 | Select-Object -First 1
    $ErrorActionPreference = $oldEAP
    Write-Host "Found Java: $javaVersionStr" -ForegroundColor Green
    
    # Robustly find the real JDK root directory (bypassing symlinked Oracle javapath)
    $javaRootDir = ""
    if (Test-Path "C:\Program Files\Java") {
        $jdkFolder = Get-ChildItem -Path "C:\Program Files\Java" -Directory -Filter "jdk-*" | Select-Object -First 1
        if ($jdkFolder) {
            $javaRootDir = $jdkFolder.FullName
        }
    }
    if (-not $javaRootDir -and (Test-Path "HKLM:\SOFTWARE\JavaSoft\JDK")) {
        $javaRootDir = (Get-ItemProperty -Path "HKLM:\SOFTWARE\JavaSoft\JDK").JavaHome
    }
    if (-not $javaRootDir) {
        $javaBinDir = Split-Path (Get-Command java).Source -Parent
        $javaRootDir = Split-Path $javaBinDir -Parent
    }
    
    $env:JAVA_HOME = $javaRootDir
    Write-Host "Setting JAVA_HOME to: $env:JAVA_HOME" -ForegroundColor Gray
} else {
    Write-Host "[ERROR] Java Runtime Environment not found. Please install JDK 21 or higher." -ForegroundColor Red
    exit 1
}

# ── Step 2: Locate or Download Apache Maven ───────────────────────────────
Write-Host ""
Write-Host "Locating Maven..." -ForegroundColor Cyan
$mvnExe = "mvn"

# Check if global mvn exists
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    # If not global, check if we already downloaded a portable version
    $portableMvnDir = Join-Path $projectDir ".maven"
    $portableMvnBin = Join-Path $portableMvnDir "apache-maven-3.9.6\bin\mvn.cmd"
    
    if (Test-Path $portableMvnBin) {
        Write-Host "Found portable Maven at: $portableMvnBin" -ForegroundColor Green
        $mvnExe = $portableMvnBin
    } else {
        Write-Host "Maven not found in system PATH." -ForegroundColor Yellow
        Write-Host "Automatically downloading a portable Apache Maven to build the project..." -ForegroundColor Cyan
        
        New-Item -ItemType Directory -Force -Path $portableMvnDir | Out-Null
        $zipPath = Join-Path $portableMvnDir "maven.zip"
        $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
        
        Write-Host "Downloading Maven from $mavenUrl..." -ForegroundColor Gray
        Invoke-WebRequest -Uri $mavenUrl -OutFile $zipPath
        
        Write-Host "Extracting Maven..." -ForegroundColor Gray
        Expand-Archive -Path $zipPath -DestinationPath $portableMvnDir -Force
        
        Remove-Item $zipPath -Force
        
        if (Test-Path $portableMvnBin) {
            Write-Host "Portable Maven successfully set up!" -ForegroundColor Green
            $mvnExe = $portableMvnBin
        } else {
            Write-Host "[ERROR] Failed to set up portable Maven." -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "Found global Maven in system PATH." -ForegroundColor Green
}

# ── Step 3: Run the Application ───────────────────────────────────────────
Write-Host ""
Write-Host "=== Compiling and Launching Drawing App... ===" -ForegroundColor Cyan
Write-Host "Running command: $mvnExe clean javafx:run"
Write-Host ""

& $mvnExe clean javafx:run
