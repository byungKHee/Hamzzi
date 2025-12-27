#!/bin/bash
set -euo pipefail

# 1. 환경 설정
PROJECT_DIR="/Users/bh/Desktop/CS/Hamzzi"
# GraalVM 25 경로 (사용자 설정 유지)
GRAAL_HOME="/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home"
OUT_BIN="$PROJECT_DIR/hamzzi"

cd "$PROJECT_DIR"

echo "[1/5] 환경 확인: GraalVM 25"
export JAVA_HOME="$GRAAL_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
native-image --version

echo "[2/5] JAR 빌드: Gradle (Java 21 사용)"
# 맥 시스템에서 Java 21 경로를 자동으로 찾습니다.
if ! JAVA_HOME_FOR_GRADLE=$(/usr/libexec/java_home -v 21 2>/dev/null); then
    echo "❌ Java 21이 설치되어 있지 않습니다. '/usr/libexec/java_home -V'로 확인하세요."
    exit 1
fi

# Gradle이 21 버전으로 ShadowJar를 생성하도록 강제합니다.
JAVA_HOME=$JAVA_HOME_FOR_GRADLE ./gradlew clean shadowJar

echo "[3/5] 빌드 파일 검증"
# 생성된 JAR 파일 이름을 정확히 확인하세요. (프로젝트 설정에 따라 다를 수 있음)
JAR_PATH="$PROJECT_DIR/build/libs/Hamzzi-1.0-SNAPSHOT-all.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다: $JAR_PATH"
    exit 1
fi
echo "✅ Fat JAR 생성 완료: $(ls -lh "$JAR_PATH" | awk '{print $5}')"

echo "[4/5] 네이티브 이미지 생성 (GraalVM 25)"
rm -f "$OUT_BIN"
# --no-fallback 옵션을 통해 의존성 누락 시 빌드 시점에 에러를 잡습니다.
native-image -jar "$JAR_PATH" "$OUT_BIN" \
    --no-fallback \
    --initialize-at-build-time \
    -H:+ReportExceptionStackTraces

echo "[5/5] 최종 마무리"
chmod +x "$OUT_BIN"
# 맥의 격리 속성(Quarantine) 해제
xattr -d com.apple.quarantine "$OUT_BIN" 2>/dev/null || true

echo "----------------------------------------"
echo "✅ 빌드 성공! 바이너리 위치: $OUT_BIN"
echo "이제 En Croissant에서 이 파일을 직접 선택하세요."