REM Kill previous instance of this bat file
taskkill /F /FI "WindowTitle eq WSL NetHack" /T>nul
REM Title the instance so it can be identified later
TITLE WSL NetHack>nul
wsl ~ -d Ubuntu-22.04 -e bash -c "bash /mnt/c/Users/gerard/MegaDrive/Documents/M2-Thesis/Code/iv4xr-nethack/server-python/start.sh"
