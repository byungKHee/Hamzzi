# 🐹 Hamzzi Engine

**Hamzzi** is a cute chess engine written in Java.

## 🎯 Goal

- **Ultimate Objective**: Defeat **Nora (Elo 2200)** on [chess.com](https://chess.com).

## 🏆 Bot Clear Status

| Bot Name   | Elo  | Status         | Note       |
| :--------- | :--- | :------------- | :--------- |
| **Martin** | 250  | ✅ Cleared     | 2025.12.28 |
| **Elani**  | 400  | ✅ Cleared     | 2025.12.28 |
| **Janjay** | 700  | ✅ Cleared     | 2025.12.28 |
| **Maria**  | 1000 | ✅ Cleared     | 2025.12.28 |
| **Nelson** | 1300 | ✅ Cleared     | 2025.12.28 |
| **Wendy**  | 1500 | ✅ Cleared     | 2026.02.07 |
| **Wally**  | 1800 | ✅ Cleared     | 2025.02.07 |
| **Li**     | 2000 | 🏗 In Progress |            |
| **Nora**   | 2200 | 🏗 In Progress |            |

## Version

0.1.0

## Local run (quick)

Requirements: Java (JDK 11+), macOS, Gradle wrapper included.

1. Build the fat jar:
   - Run `./gradlew shadowJar`

2. Prepare the runner:
   - Place `run.sh` next to the generated jar (for example in `build/libs/`)
   - Make it executable: `chmod +x run.sh`

3. Run the engine:
   - Execute `./run.sh`

The `run.sh` script should invoke the produced shadow jar. After these steps the engine will start locally.
