# GanttApp - How to Run
**Date:** 2026-02-10
---
## ✅ Correct Way to Run GanttApp
### Option 1: IntelliJ IDEA (Recommended)
**Run Configuration:** `GanttAppRunner`
1. Open IntelliJ IDEA
2. Find "Run Configurations" dropdown (top right)
3. Select `GanttAppRunner`
4. Click ▶️ Run button
**What it does:**
- Runs `mvn clean compile exec:java@gantt`
- Uses Maven to manage all dependencies
- Correctly sets up JPMS module path
- Applies UI scaling (1.5x)
---
### Option 2: Command Line (Maven)
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean compile exec:java@gantt
```
**Alternative (default execution - DashApp):**
```bash
mvn clean compile exec:java
```
---
## ❌ Don't Run Directly with Java
**This will fail:**
```bash
java -m de.ruu.app.jeeeraaah.frontend.ui.fx/de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner
```
**Why it fails:**
- Manual module path setup is complex
- Easy to miss required modules
- IntelliJ's auto-generated command may be incorrect
**Use Maven instead!** Maven handles all module path configuration automatically.
---
## 🔧 Available Run Configurations
### DashAppRunner (Default)
```bash
mvn exec:java
# or
mvn exec:java@default-cli
```
**Main Class:** `de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner`
### GanttAppRunner
```bash
mvn exec:java@gantt
```
**Main Class:** `de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner`
---
## 📝 pom.xml Configuration
Both apps are configured in `root/app/jeeeraaah/frontend/ui/fx/pom.xml`:
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <!-- DashApp -->
        <execution>
            <id>default-cli</id>
            <goals>
                <goal>java</goal>
            </goals>
            <configuration>
                <mainClass>de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner</mainClass>
            </configuration>
        </execution>
        <!-- GanttApp -->
        <execution>
            <id>gantt</id>
            <goals>
                <goal>java</goal>
            </goals>
            <configuration>
                <mainClass>de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner</mainClass>
            </configuration>
        </execution>
    </executions>
</plugin>
```
---
## 🐛 Troubleshooting
### Module not found error?
**Error:**
```
java.lang.module.FindException: Module de.ruu.app.jeeeraaah.frontend.common.mapping not found
```
**Solution:** Use Maven, not direct Java execution:
```bash
mvn clean compile exec:java@gantt
```
### Build fails?
**Clean and rebuild:**
```bash
cd /home/r-uu/develop/github/main/root
mvn clean install
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean compile exec:java@gantt
```
### UI scaling issues?
Both apps use 1.5x scaling by default (`glass.gtk.uiScale=1.5`).
To change:
1. Edit `pom.xml`
2. Find `<systemProperty>` with key `glass.gtk.uiScale`
3. Change `<value>1.5</value>` to desired scale
---
## 📚 Related Documentation
- [GETTING-STARTED.md](../../../../../GETTING-STARTED.md) - Complete setup guide
- [JPMS-RUN-CONFIGURATIONS.md](../../../../../JPMS-RUN-CONFIGURATIONS.md) - JPMS configuration
- [INTELLIJ-CACHE-CLEANUP.md](../../../../../INTELLIJ-CACHE-CLEANUP.md) - IntelliJ issues
---
**Summary:** Always use Maven (`mvn exec:java@gantt`) or IntelliJ Run Configurations. Never run with `java -m` directly!
---
**Last updated:** 2026-02-10
