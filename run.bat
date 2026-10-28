@echo off
title Student Result Management System
cd /d "%~dp0"

if exist "student-result-system.jar" (
    echo Starting Student Result Management System...
    start "" javaw -jar "student-result-system.jar"
    if %ERRORLEVEL% NEQ 0 (
        java -jar "student-result-system.jar"
        pause
    )
    exit /b
)

if exist "target\student-result-system.jar" (
    echo Starting Student Result Management System...
    start "" javaw -jar "target\student-result-system.jar"
    if %ERRORLEVEL% NEQ 0 (
        java -jar "target\student-result-system.jar"
        pause
    )
    exit /b
)

echo Executable JAR not found. Building package using Maven...
call mvnw.cmd package -DskipTests
if exist "target\student-result-system.jar" (
    start "" javaw -jar "target\student-result-system.jar"
) else (
    echo Build failed. Please ensure Java 21+ and Maven are configured.
    pause
)
