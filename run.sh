#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

if [ -f "student-result-system.jar" ]; then
    java -jar student-result-system.jar
elif [ -f "target/student-result-system.jar" ]; then
    java -jar target/student-result-system.jar
else
    echo "Executable JAR not found. Building with Maven..."
    mvn package -DskipTests
    java -jar target/student-result-system.jar
fi

