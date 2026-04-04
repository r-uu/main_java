<#
.SYNOPSIS
    Configures Windows Defender exclusions for the JEEERAAAH / r-uu development environment.

.DESCRIPTION
    Adds relevant exclusions for Java/Maven/WSL2/IntelliJ/Flutter/Android development.
    Removes outdated exclusions (paths that no longer exist on disk).
    Safe to run multiple times (idempotent).

.NOTES
    Requires Windows Administrator privileges.

    Run from WSL2 terminal:
        powershell.exe -ExecutionPolicy Bypass -Command "
            Start-Process powershell.exe -Verb RunAs -Wait \
            -ArgumentList '-ExecutionPolicy Bypass -NoProfile -File C:\Users\r-uu\tmp\Configure-WindowsDefender.ps1'
        "

    Or from Windows: Right-click PowerShell → Run as Administrator, then:
        Set-ExecutionPolicy Bypass -Scope Process -Force
        & "$env:USERPROFILE\path\to\Configure-WindowsDefender.ps1"

.LAST-VERIFIED
    2026-04-04 — Applied successfully, resulting state:
    PATH  : \\wsl.localhost\Ubuntu\home\r-uu\develop\github\main
            C:\Program Files\JetBrains
            C:\Program Files\nodejs
            C:\Users\r-uu\.m2
            C:\Users\r-uu\AppData\Local\Android\Sdk
            C:\Users\r-uu\AppData\Local\Flutter
            C:\Users\r-uu\AppData\Local\Google\AndroidStudio2025.1.1
            C:\Users\r-uu\AppData\Local\JetBrains
            C:\Users\r-uu\AppData\Local\JetBrains\IntelliJIdea2026.1
            C:\Users\r-uu\AppData\Local\Packages\CanonicalGroupLimited.Ubuntu_79rhkp1fndgsc
            C:\Users\r-uu\AppData\Roaming\JetBrains
            C:\Users\r-uu\StudioProjects\flutter1
    PROC  : C:\Users\r-uu\AppData\Local\DBeaver\dbeaver.exe
            fsnotifier.exe  idea64.exe  java.exe  javaw.exe  node.exe
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Continue'   # don't abort on non-fatal errors

$userProfile = $env:USERPROFILE

# ---------------------------------------------------------------------------
# Helper functions
# ---------------------------------------------------------------------------
function Write-Section([string]$title) {
    Write-Host "`n=== $title ===" -ForegroundColor Cyan
}

function Add-PathExclusion([string]$path, [string]$reason) {
    if (-not (Test-Path $path -ErrorAction SilentlyContinue)) {
        Write-Host "  SKIP (not found): $path" -ForegroundColor DarkGray
        return
    }
    $current = (Get-MpPreference).ExclusionPath
    if ($current -contains $path) {
        Write-Host "  ALREADY SET: $path" -ForegroundColor DarkGray
    } else {
        Add-MpPreference -ExclusionPath $path
        Write-Host "  ADDED [$reason]: $path" -ForegroundColor Green
    }
}

function Add-ProcessExclusion([string]$process, [string]$reason) {
    $current = (Get-MpPreference).ExclusionProcess
    if ($current -contains $process) {
        Write-Host "  ALREADY SET: $process" -ForegroundColor DarkGray
    } else {
        Add-MpPreference -ExclusionProcess $process
        Write-Host "  ADDED [$reason]: $process" -ForegroundColor Green
    }
}

# ---------------------------------------------------------------------------
# Show current state before changes
# ---------------------------------------------------------------------------
Write-Section "Current Exclusions (before changes)"
$pref = Get-MpPreference
Write-Host "Paths     : $($pref.ExclusionPath     -join '; ')" -ForegroundColor Gray
Write-Host "Processes : $($pref.ExclusionProcess  -join '; ')" -ForegroundColor Gray

# ---------------------------------------------------------------------------
# 1. WSL2 — entire Ubuntu package directory
#    MOST IMPORTANT: covers ext4.vhdx (all project files, Maven cache, Docker
#    volumes, build output inside WSL2 live here).
#    The UNC exclusion (\\wsl.localhost\...) covers Windows-side access via 9P.
# ---------------------------------------------------------------------------
Write-Section "WSL2 Exclusions"

$wslPackages = Get-ChildItem "$userProfile\AppData\Local\Packages" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like 'CanonicalGroupLimited*' }
foreach ($pkg in $wslPackages) {
    Add-PathExclusion $pkg.FullName "WSL2 Ubuntu package (ext4.vhdx)"
}

# UNC access path (used by Windows tools including IntelliJ to access WSL files)
Add-PathExclusion "\\wsl.localhost\Ubuntu\home\r-uu\develop" "WSL2 project root via UNC"

# ---------------------------------------------------------------------------
# 2. Maven local repository (on the Windows side — used by IntelliJ directly)
# ---------------------------------------------------------------------------
Write-Section "Maven Exclusions"

Add-PathExclusion "$userProfile\.m2" "Maven local repository"

# ---------------------------------------------------------------------------
# 3. IntelliJ IDEA — installation, caches, indexes, logs
# ---------------------------------------------------------------------------
Write-Section "IntelliJ IDEA Exclusions"

Add-PathExclusion "$userProfile\AppData\Local\JetBrains"   "JetBrains local caches/indexes (all versions)"
Add-PathExclusion "$userProfile\AppData\Roaming\JetBrains" "JetBrains roaming config (all versions)"

# All installed JetBrains IDEs in Program Files
$jetbrainsDir = 'C:\Program Files\JetBrains'
if (Test-Path $jetbrainsDir) {
    Add-PathExclusion $jetbrainsDir "JetBrains Program Files (all IDEs)"
    Get-ChildItem $jetbrainsDir -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        Add-PathExclusion $_.FullName "JetBrains IDE: $($_.Name)"
    }
}

# ---------------------------------------------------------------------------
# 4. Android Studio + Android SDK (Flutter/Android development)
# ---------------------------------------------------------------------------
Write-Section "Android / Flutter Exclusions"

Add-PathExclusion "$userProfile\AppData\Local\Android\Sdk"  "Android SDK"
Add-PathExclusion "$userProfile\AppData\Local\Flutter"       "Flutter SDK"
Add-PathExclusion "$userProfile\StudioProjects"              "Android Studio projects"

# Android Studio installations (all versions found in AppData)
Get-ChildItem "$userProfile\AppData\Local\Google" -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like 'AndroidStudio*' } |
    ForEach-Object { Add-PathExclusion $_.FullName "Android Studio: $($_.Name)" }

# ---------------------------------------------------------------------------
# 5. Node.js
# ---------------------------------------------------------------------------
Write-Section "Node.js Exclusions"

Add-PathExclusion 'C:\Program Files\nodejs'               "Node.js installation"
Add-PathExclusion "$userProfile\AppData\Roaming\npm"      "npm global packages"
Add-PathExclusion "$userProfile\AppData\Local\npm-cache"  "npm cache"

Add-ProcessExclusion 'node.exe' "Node.js runtime"

# ---------------------------------------------------------------------------
# 6. Java and IntelliJ processes
# ---------------------------------------------------------------------------
Write-Section "Process Exclusions"

Add-ProcessExclusion 'java.exe'        "Java runtime"
Add-ProcessExclusion 'javaw.exe'       "Java runtime (windowed / JavaFX)"
Add-ProcessExclusion 'idea64.exe'      "IntelliJ IDEA 64-bit"
Add-ProcessExclusion 'idea.exe'        "IntelliJ IDEA"
Add-ProcessExclusion 'fsnotifier.exe'  "IntelliJ file watcher"
Add-ProcessExclusion 'jcef_helper.exe' "IntelliJ JCEF browser"

# ---------------------------------------------------------------------------
# 7. DBeaver (database tool used with PostgreSQL in this project)
# ---------------------------------------------------------------------------
Write-Section "DBeaver Exclusions"

$dbeaver = "$userProfile\AppData\Local\DBeaver\dbeaver.exe"
if (Test-Path $dbeaver) {
    Add-ProcessExclusion $dbeaver "DBeaver database tool"
}

# ---------------------------------------------------------------------------
# 8. Git for Windows
# ---------------------------------------------------------------------------
Write-Section "Git Exclusions"

foreach ($p in @('C:\Program Files\Git', 'C:\Program Files (x86)\Git')) {
    Add-PathExclusion $p "Git for Windows"
}

# ---------------------------------------------------------------------------
# 9. Remove outdated exclusions (paths that no longer exist on disk)
# ---------------------------------------------------------------------------
Write-Section "Removing Outdated Path Exclusions"

$removedCount = 0
$currentPaths = (Get-MpPreference).ExclusionPath
foreach ($path in $currentPaths) {
    if ([string]::IsNullOrWhiteSpace($path)) { continue }
    # Skip UNC paths (Test-Path can be slow/unreliable for UNC in admin context)
    if ($path.StartsWith('\\')) { continue }
    if (-not (Test-Path $path -ErrorAction SilentlyContinue)) {
        try {
            Remove-MpPreference -ExclusionPath $path
            Write-Host "  REMOVED (path no longer exists): $path" -ForegroundColor Yellow
            $removedCount++
        } catch {
            Write-Host "  WARNING: Could not remove '$path': $_" -ForegroundColor Red
        }
    }
}

# Remove known irrelevant process exclusions (.NET/Visual Studio — not used here)
$irrelevantProcesses = @('msbuild.exe','devenv.exe','vbexpress.exe','vcexpress.exe',
                         'csexpress.exe','VBCSCompiler.exe','csc.exe','vbc.exe')
$currentProcesses = (Get-MpPreference).ExclusionProcess
foreach ($proc in $currentProcesses) {
    if ([string]::IsNullOrWhiteSpace($proc)) { continue }
    if ($irrelevantProcesses -contains $proc) {
        try {
            Remove-MpPreference -ExclusionProcess $proc
            Write-Host "  REMOVED (irrelevant .NET/VS process): $proc" -ForegroundColor Yellow
            $removedCount++
        } catch {
            Write-Host "  WARNING: Could not remove process '$proc': $_" -ForegroundColor Red
        }
    }
}

if ($removedCount -eq 0) {
    Write-Host "  Nothing to remove — all existing exclusions are still valid." -ForegroundColor DarkGray
}

# ---------------------------------------------------------------------------
# Final state summary
# ---------------------------------------------------------------------------
Write-Section "Final Exclusions (after changes)"
$pref = Get-MpPreference

Write-Host "`nPath exclusions:" -ForegroundColor White
$pref.ExclusionPath | Sort-Object | ForEach-Object { Write-Host "  - $_" }

Write-Host "`nProcess exclusions:" -ForegroundColor White
$pref.ExclusionProcess | Sort-Object | ForEach-Object { Write-Host "  - $_" }

Write-Host "`n[DONE] Windows Defender configuration complete." -ForegroundColor Green

