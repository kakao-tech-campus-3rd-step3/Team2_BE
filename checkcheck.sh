#!/bin/bash
set -e #중간에 에러 나면 바로 종료

./gradlew :spotlessApply
./gradlew codeCheck
./gradlew build --stacktrace
