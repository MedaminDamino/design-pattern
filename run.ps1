# run.ps1 – Compile and launch the JavaFX Drawing Application
# Usage: .\run.ps1
#
# The JavaFX SDK path contains an accented character (é) which causes issues
# with some tools. This script resolves the path via Get-Item wildcard and
# builds the full classpath including SQLite JDBC from the local .m2 repo.

$ErrorActionPreference = "Stop"

$javaExe  = "C:\Program Files\Java\jdk-23\bin\java.exe"
$javacExe = "C:\Program Files\Java\jdk-23\bin\javac.exe"
$mvnExe   = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd"
$projectDir = $PSScriptRoot

# ── Resolve JavaFX SDK path (wildcard bypasses encoding issue with é) ──────
$glid2Root = Join-Path $projectDir ".."
$sdkParent = (Get-Item (Join-Path $glid2Root "Syst*") |
              Where-Object { $_.PSIsContainer } |
              Select-Object -First 1).FullName
$javafxLib = Join-Path $sdkParent "javafx-sdk-25\lib"

if (-not (Test-Path $javafxLib)) {
    Write-Host "[ERROR] JavaFX SDK not found at: $javafxLib" -ForegroundColor Red
    exit 1
}

# ── Build full classpath using Maven ─────────────────────────────────────
Write-Host ""
Write-Host "=== Step 1: Building and collecting dependencies... ===" -ForegroundColor Cyan
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
& $mvnExe compile dependency:build-classpath "-Dmdep.outputFile=cp.txt" "-q"
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] Compilation or dependency resolution failed." -ForegroundColor Red; exit 1 }

$classes = Join-Path $projectDir "target\classes"
$depsCp = Get-Content (Join-Path $projectDir "cp.txt") -ErrorAction SilentlyContinue
$cp = if ($depsCp) { "$classes;$depsCp" } else { $classes }
Write-Host "Compilation OK." -ForegroundColor Green

# ── Step 3: Launch ────────────────────────────────────────────────────────
Write-Host ""
Write-Host "=== Step 2: Launching Drawing App... ===" -ForegroundColor Cyan
Write-Host "  JDK 23:  $javaExe"
Write-Host "  FX lib:  $javafxLib"
Write-Host "  Deps cp: $depsCp"
Write-Host ""

& $javaExe `
    "--module-path" $javafxLib `
    "--add-modules"  "javafx.controls,javafx.fxml" `
    "--add-opens"    "javafx.fxml/javafx.fxml=ALL-UNNAMED" `
    "-cp"            $cp `
    "app.MainApp"
