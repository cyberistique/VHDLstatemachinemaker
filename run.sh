#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./run.sh [output_base_name]
#
# Optional JavaFX:
#   export FX="/path/to/javafx-sdk/lib"
#   ./run.sh my_output

OUT_NAME="${1:-fsm}"

javac src/*.java

if [[ -n "${FX:-}" ]]; then
  exec java --module-path "$FX" --add-modules javafx.controls -cp src MachineViewer "$OUT_NAME"
else
  exec java -cp src MachineViewer "$OUT_NAME"
fi

