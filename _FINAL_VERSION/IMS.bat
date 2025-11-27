@echo off
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "JAVAFX_HOME=C:\javafx-sdk-21"

echo Starting IMS Server...
start "IMS Server" "%JAVA_HOME%\bin\java.exe" -jar "ims-server.jar"

REM give the server a few seconds to start listening on port 8888
timeout /t 3 /nobreak >nul

echo Starting IMS Client...
"%JAVA_HOME%\bin\javaw.exe" ^
  --module-path "%JAVAFX_HOME%\lib" ^
  --add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base ^
  -jar "ims.jar"

echo Client launched. Close the 'IMS Server' console when you're done.
