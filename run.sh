#!/bin/bash

# 1. 스크립트 파일이 위치한 디렉토리(루트) 경로 확보
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 2. ShadowJar 위치 지정 (방금 tree에서 확인된 경로)
JAR_PATH="$DIR/build/libs/Hamzzi-1.0-SNAPSHOT-all.jar"

# 3. 자바 실행
# shadowJar는 모든 의존성을 포함하므로 -cp 대신 -jar를 쓰면 됩니다.
exec java -Dfile.encoding=UTF-8 -jar "$JAR_PATH"