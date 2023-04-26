@echo off
setlocal

@REM Get the absolute directory path of the current batch file
set BASEDIR=%~dp0
set BASEDIR=%BASEDIR:~0,-1%

@REM Convert the Windows-style path to a WSL-compatible path
for /f "usebackq delims=" %%I in (`wsl wslpath -a %BASEDIR:\=/%`) do set WSL_BASEDIR=%%I

REM Kill previous instance of this bat file
taskkill /F /FI "WindowTitle eq WSL NetHack" /T>nul
REM Title the instance so it can be identified later
TITLE WSL NetHack
wsl ~ -d Ubuntu-22.04 -e bash -c "bash %WSL_BASEDIR%/start.sh"

endlocal
