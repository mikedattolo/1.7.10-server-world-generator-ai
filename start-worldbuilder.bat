@echo off
setlocal

set NOWAIT=0
if /I "%~1"=="--no-wait" set NOWAIT=1

set APPDIR=%~dp0

cd /d "%APPDIR%"
echo [WorldBuilder] Starting setup and launch...

where java >nul 2>nul
if errorlevel 1 (
  echo [WorldBuilder] Java JDK not found on PATH.
  where winget >nul 2>nul
  if errorlevel 1 (
    echo [WorldBuilder] winget not found. Please install Java JDK 8+ and re-run this script.
    call :hold
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

where javac >nul 2>nul
if errorlevel 1 (
  echo [WorldBuilder] Java runtime found, but javac was not found.
  echo [WorldBuilder] Please install a Java JDK, not only a Java runtime, then re-run this script.
  call :hold
  exit /b 1
)

if not exist "%APPDIR%mvnw.cmd" (
  echo [WorldBuilder] Maven Wrapper is missing: %APPDIR%mvnw.cmd
  echo [WorldBuilder] Download the latest project files and try again.
  call :hold
  exit /b 1
)

echo [WorldBuilder] Building project classes...
echo [WorldBuilder] Maven will be downloaded automatically on first run if needed.
call "%APPDIR%mvnw.cmd" -q -DskipTests package
if errorlevel 1 (
  echo [WorldBuilder] Build failed. Check errors above.
  call :hold
  exit /b 1
)

set "APPJAR="
for /f "delims=" %%F in ('dir /b /a:-d "%APPDIR%target\server-world-generator-ai-*.jar" 2^>nul') do set "APPJAR=%APPDIR%target\%%F"
if not defined APPJAR (
  echo [WorldBuilder] Build finished, but no application jar was found in target.
  call :hold
  exit /b 1
)

echo [WorldBuilder] Launching GUI...
where javaw >nul 2>nul
if errorlevel 1 (
  java -jar "%APPJAR%" --gui
) else (
  start "WorldBuilder GUI" /B javaw -jar "%APPJAR%" --gui
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
exit /b 0

:hold
if "%NOWAIT%"=="1" goto :eof
echo.
pause
goto :eof
