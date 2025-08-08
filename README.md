# 📊 Standard Deviation Calculator — D3 F8 (v 3.0.0)

A **from-scratch** Java Swing application that computes the population standard deviation of an integer list.  
Built for the COMP *SOEN 6011* “D3 F8” deliverable.  
No built-in math helpers are used—mean, variance, and √ are implemented manually.

---

## ✨ Key features
| Area | Details |
|------|---------|
|Pure-Java core|`Statistics.java` provides `mean → variance → stdDev`, plus a Newton–Raphson `sqrt`—all unit-tested.|
|Accessible GUI|Keyboard-navigable, WCAG-AA colour contrast, screen-reader descriptions, FR/EN localisation.|
|Quality gate|Google Checkstyle + PMD; every violation fixed.|
|Debug proof|JDB breakpoint screenshots included (see `/doc`).|
|Tests|JUnit 5 with >90 % line coverage (`./gradlew test jacocoTestReport`).|
|CI-ready|Gradle Wrapper, semantic version tag `v3.0.0`, MIT license.|
|Poster|Mind-map (style guide), pipeline diagram, and tool evidence in `/poster`.|

---

## 🏗 Getting started

### Prerequisites
* **JDK 17** or later  
* **Git** & **Gradle 7+** (wrapper included—no global install needed)

### Clone & build
```bash
git clone https://github.com/<you>/stddev-d3-f8.git
cd stddev-d3-f8
./gradlew clean build
```

### Run the app
```bash
./gradlew run
# or, after building:
java -jar build/libs/stddev-gui-3.0.0.jar
```

---

## 🖥 Using the GUI
1. Enter integers separated by **spaces / commas / newlines**.  
2. Press **Alt + C** or click **Calculate**.  
3. The app shows:
   * σ (standard deviation)  
   * μ (mean)  
   * Variance  
   * Step-by-step workings in an expandable panel  

Invalid input triggers inline and dialog errors—both screen-reader friendly.  

---

## 🗂 Project structure
```
.
├── src
│   ├── main
│   │   └── java
│   │       ├── gui          # StdGUI.java (Swing front-end)
│   │       └── core         # Statistics.java (logic)
│   └── test
│       └── java             # StatisticsTest.java
├── build.gradle
├── VERSION                  # 3.0.0
├── .checkstyle.xml          # Google ruleset
├── pmd-ruleset.xml
├── README.md                # ← you are here
└── LICENSE                  # MIT
```

---

## 📐 Coding style
* **Google Java Style** (see mind-map in `/doc/style-mindmap.png`)  
* Enforced via Checkstyle + PMD in `./gradlew check`

---

## 🤝 Contributing
Pull requests welcome! Please:
1. Open an issue first if it’s a major change.  
2. Follow the style guide; run `./gradlew check test` before committing.  
3. Use **conventional commits** (`feat: …`, `fix: …`, `chore: …`).

---

## 📝 License
Released under the **MIT License** – see `LICENSE` for details.

---

<p align="center">Built with ❤️ in Java and tested on OpenJDK 17.</p>
