@echo off
setlocal
set "CP=build;lib/*"
set "MAIN=client.ui.ClientGUI"

if not exist build call ".\build.cmd" || exit /b 1

REM permite override do host/porta pela linha de comando
REM uso: run_client.cmd 192.168.1.10 9090
if not "%~1"=="" set "OVERRIDE_HOST=%~1"
if not "%~2"=="" set "OVERRIDE_PORT=%~2"

if defined OVERRIDE_HOST (
  echo server.host=%OVERRIDE_HOST%> client.properties
  if defined OVERRIDE_PORT (echo server.port=%OVERRIDE_PORT%>> client.properties) else (echo server.port=9090>> client.properties)
  echo [CLIENT] usando client.properties gerado: %OVERRIDE_HOST%:%OVERRIDE_PORT%
)

java -cp "%CP%" %MAIN%
