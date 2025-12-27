plugins {
    id("java")
    // ShadowJar 플러그인: 모든 의존성을 하나의 JAR로 묶어줍니다.
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        // Gradle 빌드에 사용할 자바 버전을 21로 고정합니다.
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // chesslib: implementation으로 지정해야 shadowJar에 포함됩니다.
    implementation("com.github.bhlangonijr:chesslib:1.3.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    // 메인 클래스 경로 확인 (패키지명 포함)
    manifest {
        attributes["Main-Class"] = "hamzzi.Main"
    }
    // 모든 의존성이 포함된 파일임을 명시 (Hamzzi-1.0-SNAPSHOT-all.jar)
    archiveClassifier.set("all")

    // 기존 파일을 덮어쓰도록 설정
    isZip64 = true
}