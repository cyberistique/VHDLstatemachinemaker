@echo off
setlocal enabledelayedexpansion

set OUT_NAME=%1
if "%OUT_NAME%"=="" set OUT_NAME=fsm

javac src\*.java
if errorlevel 1 exit /b 1

if not "%FX%"=="" (
  java --module-path "%FX%" --add-modules javafx.controls -cp src MachineViewer %OUT_NAME%
) else (
  java -cp src MachineViewer %OUT_NAME%
)

`