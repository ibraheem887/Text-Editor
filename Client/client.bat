@echo off
setlocal

cd %~dp0

REM Set the installation directory
set "INSTALL_DIR=C:\Program Files\TextEditorClient"

REM Create installation directory
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

REM Copy JAR file
copy /Y "client.jar" "%INSTALL_DIR%"

REM Create logs directory
if not exist "%INSTALL_DIR%\logs" mkdir "%INSTALL_DIR%\logs"
if not exist "%INSTALL_DIR%\resources" mkdir "%INSTALL_DIR%\resources"
copy /Y "resources\log4j2.xml" "%INSTALL_DIR%\resources"
echo Software Installed > "%INSTALL_DIR%\logs\application.log"

echo Installation completed successfully.
echo Please run the application using java -jar "%INSTALL_DIR%\client.jar"
java -jar "%INSTALL_DIR%\client.jar"
pause
endlocal