#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
echo "[WorldBuilder] Starting setup and launch..."

if ! command -v java >/dev/null 2>&1; then
  echo "[WorldBuilder] Java not found on PATH. Install Java 8+ and retry."
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "[WorldBuilder] Maven not found on PATH. Install Maven and retry."
  exit 1
fi

echo "[WorldBuilder] Building project classes..."
mvn -q -DskipTests package

if [[ -z "${DISPLAY:-}" && -z "${WAYLAND_DISPLAY:-}" ]]; then
  echo "[WorldBuilder] Build complete. No graphical display detected in this shell."
  echo "[WorldBuilder] Run this script on a desktop session to launch the GUI."
  exit 0
fi

echo "[WorldBuilder] Launching GUI..."
java -cp target/classes com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp
