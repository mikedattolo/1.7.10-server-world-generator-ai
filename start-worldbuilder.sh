#!/usr/bin/env bash
set -uo pipefail

NO_WAIT=0
if [[ "${1:-}" == "--no-wait" ]]; then
  NO_WAIT=1
fi

hold() {
  if [[ "$NO_WAIT" == "1" ]]; then
    return
  fi
  if [[ -t 0 ]]; then
    echo
    read -r -p "Press Enter to close..." _
  fi
}

cd "$(dirname "$0")"
echo "[WorldBuilder] Starting setup and launch..."

if ! command -v java >/dev/null 2>&1; then
  echo "[WorldBuilder] Java JDK not found on PATH. Install Java JDK 8+ and retry."
  hold
  exit 1
fi

if ! command -v javac >/dev/null 2>&1; then
  echo "[WorldBuilder] Java runtime found, but javac was not found. Install a JDK, not only a runtime."
  hold
  exit 1
fi

if [[ ! -x "./mvnw" ]]; then
  echo "[WorldBuilder] Maven Wrapper is missing or not executable: ./mvnw"
  echo "[WorldBuilder] Download the latest project files and try again."
  hold
  exit 1
fi

echo "[WorldBuilder] Building project classes..."
echo "[WorldBuilder] Maven will be downloaded automatically on first run if needed."
if ! ./mvnw -q -DskipTests package; then
  echo "[WorldBuilder] Build failed."
  hold
  exit 1
fi

APP_JAR=$(find target -maxdepth 1 -name 'server-world-generator-ai-*.jar' | head -n 1)
if [[ -z "$APP_JAR" ]]; then
  echo "[WorldBuilder] Build finished, but no application jar was found in target."
  hold
  exit 1
fi

if [[ -z "${DISPLAY:-}" && -z "${WAYLAND_DISPLAY:-}" ]]; then
  echo "[WorldBuilder] Build complete. No graphical display detected in this shell."
  echo "[WorldBuilder] Run this script in a desktop terminal session to launch the GUI."
  echo "[WorldBuilder] You can still use CLI with:"
  echo "[WorldBuilder]   java -jar $APP_JAR --help"
  hold
  exit 0
fi

echo "[WorldBuilder] Launching GUI..."
if ! java -jar "$APP_JAR" --gui; then
  echo "[WorldBuilder] Launch failed."
  hold
  exit 1
fi
