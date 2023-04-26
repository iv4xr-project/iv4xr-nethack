@REM @echo off
setlocal

@REM Change to the directory containing the repository
SET BASEDIR=%~dp0
SET BASEDIR=%BASEDIR:~0,-1%
echo %BASEDIR%
cd /d %BASEDIR%\client-java

@REM Compile the jar and package it up, then run App.java in background
start /WAIT cmd /C "mvn -Dm2.localRepository=~\.m2\repository package & exit"
start cmd /C "java -ea -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\Users\gerard\MegaDrive\Documents\M2-Thesis\Code\iv4xr-nethack\client-java\target\classes;C:\Users\gerard\.m2\repository\junit\junit\3.8.1\junit-3.8.1.jar;C:\Users\gerard\.m2\repository\com\github\iv4xr-project\aplib\1.7.0-SNAPSHOT\aplib-1.7.0-SNAPSHOT.jar;C:\Users\gerard\.m2\repository\it\unibo\alice\tuprolog\tuprolog\3.3.0\tuprolog-3.3.0.jar;C:\Users\gerard\.m2\repository\com\google\code\gson\gson\2.8.9\gson-2.8.9.jar;C:\Users\gerard\.m2\repository\org\junit\jupiter\junit-jupiter-engine\5.6.0\junit-jupiter-engine-5.6.0.jar;C:\Users\gerard\.m2\repository\org\apiguardian\apiguardian-api\1.1.0\apiguardian-api-1.1.0.jar;C:\Users\gerard\.m2\repository\org\junit\platform\junit-platform-engine\1.6.0\junit-platform-engine-1.6.0.jar;C:\Users\gerard\.m2\repository\org\opentest4j\opentest4j\1.2.0\opentest4j-1.2.0.jar;C:\Users\gerard\.m2\repository\org\junit\platform\junit-platform-commons\1.6.0\junit-platform-commons-1.6.0.jar;C:\Users\gerard\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.6.0\junit-jupiter-api-5.6.0.jar;C:\Users\gerard\.m2\repository\org\pitest\pitest-junit5-plugin\0.15\pitest-junit5-plugin-0.15.jar;C:\Users\gerard\.m2\repository\commons-beanutils\commons-beanutils\1.9.4\commons-beanutils-1.9.4.jar;C:\Users\gerard\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;C:\Users\gerard\.m2\repository\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar;C:\Users\gerard\.m2\repository\org\apache\logging\log4j\log4j-api\2.20.0\log4j-api-2.20.0.jar;C:\Users\gerard\.m2\repository\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar;C:\Users\gerard\.m2\repository\org\jetbrains\annotations\24.0.0\annotations-24.0.0.jar;C:\Users\gerard\.m2\repository\org\apache\commons\commons-configuration2\2.9.0\commons-configuration2-2.9.0.jar;C:\Users\gerard\.m2\repository\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar;C:\Users\gerard\.m2\repository\org\apache\commons\commons-text\1.10.0\commons-text-1.10.0.jar;C:\Users\gerard\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.12.7.1\jackson-databind-2.12.7.1.jar;C:\Users\gerard\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.12.7\jackson-annotations-2.12.7.jar;C:\Users\gerard\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.12.7\jackson-core-2.12.7.jar agent.App"

@REM Store the process ID (PID) of the Java process
set java_pid=%errorlevel%

wsl ~ -d Ubuntu-22.04 -e bash -c "bash /mnt/c/Users/gerard/MegaDrive/Documents/M2-Thesis/Code/iv4xr-nethack/mutating-srciror/run-server-mutating.sh"

@REM Loop until the Java process has started
@REM :loop
@REM tasklist /FI "PID eq %java_pid%" | findstr /c:"java.exe" >nul
@REM if errorlevel 1 goto loop

@REM Execute start.sh
@REM call server-python/wsl-start.sh

@REM Clean up by killing the Java process
@REM taskkill /F /PID %java_pid%

Check for error
if %errorlevel% neq 0 (
  echo An error occurred!
  pause
)
