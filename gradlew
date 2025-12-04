#!/bin/sh
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle CLI not found. Please run 'gradle wrapper' locally or install Gradle."
  exit 1
fi
