@echo off
setlocal

cd /d "%~dp0"
echo [WorldBuilder] Starting setup and launch...

where java >nul 2>nul
if errorlevel 1 (
  echo [WorldBuilder] Java not found on PATH.
  where winget >nul 2>nul
  if errorlevel 1 (
    echo [WorldBuilder] winget not found. Please install Java 8+ and re-run this script.
    pause
    exit /b 1
  )
  echo [WorldBuilder] Installing Java (Temurin 8) via winget...
  winget install -e --id EclipseAdoptium.Temurin.8.JDK --accept-package-agreements --accept-source-agreements
  if errorlevel 1 (
    echo [WorldBuilder] Failed to install Java automatically. Install Java 8+ and re-run.
    pause
    exit /b 1
  )
)

where mvn >nul 2>nul
if errorlevel 1 (
  echo [WorldBuilder] Maven not found on PATH.
  where winget >nul 2>nul
  if errorlevel 1 (
    echo [WorldBuilder] winget not found. Please install Maven and re-run this script.
    pause
    exit /b 1
  )
  echo [WorldBuilder] Installing Maven via winget...
  winget install -e --id Apache.Maven --accept-package-agreements --accept-source-agreements
  if errorlevel 1 (
    echo [WorldBuilder] Failed to install Maven automatically. Install Maven and re-run.
    pause
    exit /b 1
  )
)

echo [WorldBuilder] Building project classes...
call mvn -q -DskipTests package
if errorlevel 1 (
  echo [WorldBuilder] Build failed. Check errors above.
  pause
  exit /b 1
)

echo [WorldBuilder] Launching GUI...
java -cp target\classes com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp
if errorlevel 1 (
  echo [WorldBuilder] Launch failed.
  pause
  exit /b 1
)

endlocal
