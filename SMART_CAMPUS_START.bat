@echo off
setlocal EnableExtensions
title Smart Campus - Spring Boot

rem Always run from the folder containing this BAT file.
cd /d "%~dp0"

set "PORT=8080"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ============================================================
echo                 SMART CAMPUS
echo ============================================================
echo Project folder: %CD%
echo.

rem Verify the project and Java installation.
if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd was not found.
    echo Place this BAT file in the root Smart Campus project folder,
    echo beside pom.xml and mvnw.cmd.
    goto :fail
)

if not exist "pom.xml" (
    echo [ERROR] pom.xml was not found. This is not the project root.
    goto :fail
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java 21 was not found at:
    echo %JAVA_HOME%
    echo Edit JAVA_HOME near the top of this BAT file if Java is installed elsewhere.
    goto :fail
)

echo [1/3] Checking Java...
"%JAVA_HOME%\bin\java.exe" -version
if errorlevel 1 goto :fail
echo.

rem Admin variables are optional after the admin account has been created.
rem If they are already set in Windows, this script leaves them unchanged.
rem Do not put a real password in this BAT file.
if not defined SMART_CAMPUS_ADMIN_EMAIL (
    echo [INFO] SMART_CAMPUS_ADMIN_EMAIL is not set in this terminal.
    echo        Existing admin accounts in the H2 database can still log in.
)
if not defined SMART_CAMPUS_ADMIN_PASSWORD (
    echo [INFO] SMART_CAMPUS_ADMIN_PASSWORD is not set in this terminal.
    echo        If this is the first admin startup, configure the variables first.
)
echo.

rem Open the landing page only after the Spring Boot server responds.
echo [2/3] Waiting for the website, then opening the landing page...
start "" /b powershell -NoProfile -WindowStyle Hidden -Command "$url='http://localhost:%PORT%/'; for($i=0; $i -lt 90; $i++){ try { $r=Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2; if($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){ Start-Process $url; exit 0 } } catch {}; Start-Sleep -Seconds 2 }"

echo [3/3] Starting Smart Campus...
echo Keep this window open while using the website.
echo Press Ctrl+C to stop the application.
echo.
call ".\mvnw.cmd" spring-boot:run
set "APP_EXIT=%ERRORLEVEL%"

echo.
if not "%APP_EXIT%"=="0" (
    echo [ERROR] Smart Campus stopped with exit code %APP_EXIT%.
) else (
    echo Smart Campus has stopped.
)
pause
exit /b %APP_EXIT%

:fail
echo.
pause
exit /b 1
