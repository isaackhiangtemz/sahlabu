@echo off
setlocal
set "APP_HOME=%~dp0"
for %%i in ("%APP_HOME%") do set "APP_HOME=%%~fi"
set "APP_BASE_NAME=gradlew"
set "CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "DEFAULT_JVM_OPTS=-Xmx64m -Xms64m"

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
if errorlevel 1 exit /b %errorlevel%
endlocal
