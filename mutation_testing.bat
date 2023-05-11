@echo off
setlocal

@REM Change to the directory containing the repository
SET BASEDIR=%~dp0
SET BASEDIR=%BASEDIR:~0,-1%
cd /d %BASEDIR%\client-java

@REM Compile the jar and package it up, then run App.java in background
SET REPODIR=%USERPROFILE%\.m2\repository
start /WAIT cmd /C "mvn -Dm2.localRepository=%REPODIR% -Dmaven.test.skip package & exit"

@REM Java command retrieved from the first run line in IntelliJ for Agent
@REM start cmd /C "java -ea -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath %BASEDIR%\client-java\target\classes;%REPODIR%\junit\junit\3.8.1\junit-3.8.1.jar;%REPODIR%\com\github\iv4xr-project\aplib\1.7.0-SNAPSHOT\aplib-1.7.0-SNAPSHOT.jar;%REPODIR%\it\unibo\alice\tuprolog\tuprolog\3.3.0\tuprolog-3.3.0.jar;%REPODIR%\com\google\code\gson\gson\2.8.9\gson-2.8.9.jar;%REPODIR%\org\junit\jupiter\junit-jupiter-engine\5.6.0\junit-jupiter-engine-5.6.0.jar;%REPODIR%\org\apiguardian\apiguardian-api\1.1.0\apiguardian-api-1.1.0.jar;%REPODIR%\org\junit\platform\junit-platform-engine\1.6.0\junit-platform-engine-1.6.0.jar;%REPODIR%\org\opentest4j\opentest4j\1.2.0\opentest4j-1.2.0.jar;%REPODIR%\org\junit\platform\junit-platform-commons\1.6.0\junit-platform-commons-1.6.0.jar;%REPODIR%\org\junit\jupiter\junit-jupiter-api\5.6.0\junit-jupiter-api-5.6.0.jar;%REPODIR%\org\pitest\pitest-junit5-plugin\0.15\pitest-junit5-plugin-0.15.jar;%REPODIR%\commons-beanutils\commons-beanutils\1.9.4\commons-beanutils-1.9.4.jar;%REPODIR%\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;%REPODIR%\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar;%REPODIR%\org\apache\logging\log4j\log4j-api\2.20.0\log4j-api-2.20.0.jar;%REPODIR%\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar;%REPODIR%\org\jetbrains\annotations\24.0.0\annotations-24.0.0.jar;%REPODIR%\org\apache\commons\commons-configuration2\2.9.0\commons-configuration2-2.9.0.jar;%REPODIR%\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar;%REPODIR%\org\apache\commons\commons-text\1.10.0\commons-text-1.10.0.jar;%REPODIR%\com\fasterxml\jackson\core\jackson-databind\2.12.7.1\jackson-databind-2.12.7.1.jar;%REPODIR%\com\fasterxml\jackson\core\jackson-annotations\2.12.7\jackson-annotations-2.12.7.jar;%REPODIR%\com\fasterxml\jackson\core\jackson-core\2.12.7\jackson-core-2.12.7.jar agent.App"

@REM Convert the Windows-style path to a WSL-compatible path
for /f "usebackq delims=" %%I in (`wsl wslpath -a %BASEDIR:\=/%`) do set WSL_BASEDIR=%%I

@REM WSL run mutation testing
wsl ~ -d Ubuntu-22.04 -e bash -c "bash %WSL_BASEDIR%/mutating-srciror/run-server-mutating.sh"

@REM Check the error code WSL gave
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: WSL ended with %ERRORLEVEL%.
    pause
)

endlocal
