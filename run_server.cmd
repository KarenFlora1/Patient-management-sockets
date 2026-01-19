@echo off
setlocal
set "CP=build;lib/*"
set "MAIN=server.Server"

if not exist build call ".\build.cmd" || exit /b 1
echo [SERVER] start on %CD%
java -cp "%CP%" %MAIN%