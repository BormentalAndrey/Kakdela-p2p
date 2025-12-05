#!/usr/bin/env sh
################################################################################
##
##  Gradle start up script for UN*X
##
################################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx1024m"'

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Resolve links: $0 may be a symlink
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done

PRG_DIR=$(dirname "$PRG")

EXECUTABLE_JAR="$PRG_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$EXECUTABLE_JAR" ]; then
  echo "ERROR: gradle-wrapper.jar not found at $EXECUTABLE_JAR"
  exit 1
fi

# Execute Gradle
java -jar "$EXECUTABLE_JAR" "$@"
