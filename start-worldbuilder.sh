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
  echo "[WorldBuilder] Java not found on PATH. Install Java 8+ and retry."
  hold
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "[WorldBuilder] Maven not found on PATH. Install Maven and retry."
  hold
  exit 1
fi

echo "[WorldBuilder] Building project classes..."
if ! mvn -q -DskipTests package; then
  echo "[WorldBuilder] Build failed."
  hold
  exit 1
fi

if [[ -z "${DISPLAY:-}" && -z "${WAYLAND_DISPLAY:-}" ]]; then
  echo "[WorldBuilder] Build complete. No graphical display detected in this shell."
  echo "[WorldBuilder] Run this script in a desktop terminal session to launch the GUI."
  echo "[WorldBuilder] You can still use CLI with:"
  echo "[WorldBuilder]   java -cp target/classes com.mikedattolo.worldbuilder.RealWorldMapBuilderApp --help"
  hold
  exit 0
fi

echo "[WorldBuilder] Launching GUI..."
if ! java -cp target/classes com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp; then
  echo "[WorldBuilder] Launch failed."
  hold
  exit 1
fi
