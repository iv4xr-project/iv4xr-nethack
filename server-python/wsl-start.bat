@echo off
setlocal
REM Kill previous instance of this bat file
taskkill /F /FI "WindowTitle eq WSL NetHack" /T>nul
REM Title the instance so it can be identified later
TITLE WSL NetHack
wsl ~ -d Ubuntu-22.04 -e bash -c "bash /mnt/c/Users/gerard/MegaDrive/Documents/M2-Thesis/Code/iv4xr-nethack/server-python/start.sh"

@REM set BASEDIR=%~dp0
@REM set BASEDIR=%BASEDIR:~0,-1%
@REM set SCRIPT_PATH=(wsl wslpath -a %BASEDIR%\server-python\start.sh)
@REM for /f %%i in ('wsl wslpath -a %BASEDIR%\server-python\start.sh') do ^
@REM   echo [%%i] ^
@REM   set RESULT=%%i ^
@REM   echo %RESULT%

@REM wsl wslpath -a -m %BASEDIR%\server-python\start.sh > temp.txt
@REM set /p VARIABLE=<%BASEDIR%/temp.txt
@REM @REM for /f "tokens=*" %%a in ('wsl wslpath -a %BASEDIR%\server-python\start.sh') do set VARIABLE=%%a
@REM echo "Variable: %VARIABLE%"
@REM REM Kill previous instance of this bat file
@REM taskkill /F /FI "WindowTitle eq WSL NetHack" /T>nul
@REM REM Title the instance so it can be identified later
@REM TITLE WSL NetHack>nul
@REM wsl ~ -d Ubuntu-22.04 -e bash -c "bash %SCRIPT_PATH%"
@REM pause
