param(
  [Parameter(Position=0)]
  [string]$OutName = "fsm"
)

$ErrorActionPreference = "Stop"

javac src/*.java

if ($env:FX) {
  java --module-path $env:FX --add-modules javafx.controls -cp src MachineViewer $OutName
} else {
  java -cp src MachineViewer $OutName
}

