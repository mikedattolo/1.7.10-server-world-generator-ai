@echo off
setlocal

set NOWAIT=0
if /I "%~1"=="--no-wait" set NOWAIT=1

set APPDIR=%~dp0

:hold
if "%NOWAIT%"=="1" goto :eof
echo.
pause
goto :eof

cd /d "%APPDIR%"
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
    call :hold
    exit /b 1
  )
  where java >nul 2>nul
  if errorlevel 1 (
    echo [WorldBuilder] Java was installed, but this shell cannot see it yet.
    echo [WorldBuilder] Close this window and run the script again.
    call :hold
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
    call :hold
    exit /b 1
  )
  where mvn >nul 2>nul
  if errorlevel 1 (
    echo [WorldBuilder] Maven was installed, but this shell cannot see it yet.
    echo [WorldBuilder] Close this window and run the script again.
    call :hold
    exit /b 1
  )
)

echo [WorldBuilder] Building project classes...
call mvn -q -DskipTests package
if errorlevel 1 (
  echo [WorldBuilder] Build failed. Check errors above.
  call :hold
  exit /b 1
)

echo [WorldBuilder] Launching GUI...
where javaw >nul 2>nul
if errorlevel 1 (
  java -cp target\classes com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp
) else (
  start "WorldBuilder GUI" /B javaw -cp target\classes com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp
)

if errorlevel 1 (
  echo [WorldBuilder] Launch failed.
  call :hold
  exit /b 1
)

echo [WorldBuilder] GUI launched.
echo [WorldBuilder] If no window appeared, run this script from Command Prompt to see detailed output.
call :hold

endlocal
