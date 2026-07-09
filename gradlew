#!/usr/bin/env bash

# Self-bootstrapping Gradle Wrapper script
# Downloads gradle-wrapper.jar if missing, then executes it.

set -e

GRADLE_WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Downloading Gradle Wrapper JAR..."
    mkdir -p gradle/wrapper
    curl -sSLo "$GRADLE_WRAPPER_JAR" https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar
fi

exec java \
    -XX:MaxMetaspaceSize=256m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Xmx1024m \
    -Dorg.gradle.appname=gradlew \
    -classpath "$GRADLE_WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"