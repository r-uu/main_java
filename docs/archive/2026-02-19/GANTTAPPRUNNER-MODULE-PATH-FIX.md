# GanttAppRunner Launch Issue - Fix Summary

**Date**: 2026-02-18  
**Issue**: `Module de.ruu.app.jeeeraaah.common.api.mapping.bean.dto not found, required by de.ruu.app.jeeeraaah.frontend.ui.fx`

---

## 🔍 Root Cause

IntelliJ placed the module `de.ruu.app.jeeeraaah.common.api.mapping.bean.dto` in the **classpath** instead of the **module-path**.

**Evidence:**
```
-classpath .../root/app/jeeeraaah/common/api/mapping.bean.dto/target/classes:...
```

**Should be:**
```
-p .../root/app/jeeeraaah/common/api/mapping.bean.dto/target/classes:...
```

---

## ✅ Solution

### Option 1: Rebuild IntelliJ Project Structure (Recommended)
```bash
# 1. Invalidate IntelliJ caches
File → Invalidate Caches → Invalidate and Restart

# 2. Reimport Maven project
Right-click on root/pom.xml → Maven → Reload Project

# 3. Rebuild project
Build → Rebuild Project
```

### Option 2: Fix Run Configuration Manually
1. Open Run Configuration: `Run → Edit Configurations → GanttAppRunner`
2. In "Configuration" tab, find "VM options"
3. Ensure `--module-path` (or `-p`) includes all JPMS modules
4. Ensure `-classpath` contains ONLY non-modular JARs

### Option 3: Use Maven to Run (Always Works)
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

---

## 🎯 Verification

After fixing, verify the module is in module-path:
```bash
# Look for this line in Run Configuration output:
-p .../common/api/mapping.bean.dto/target/classes:...

# NOT this:
-classpath .../common/api/mapping.bean.dto/target/classes:...
```

---

## 🔧 Why This Happened

IntelliJ sometimes miscategorizes modules, especially after:
- Adding new modules
- Changing POM dependencies
- Cache corruption
- JPMS module reorganization (like our Phase 2/3 work)

The module has a valid `module-info.java`, so it **must** be in the module-path.

---

## 📝 Completed Fixes (Phase 3)

Before this issue occurred, we completed:
1. ✅ Cleaned up POM version tags (BOM managed)
2. ✅ Added missing lib modules to BOM
3. ✅ Optimized backend mapping `module-info.java` with targeted `opens`
4. ✅ Maven build compiles successfully

---

## 🚀 Next Steps After Fix

Once IntelliJ correctly loads the module:
1. Run GanttAppRunner from IDE
2. Verify application starts successfully
3. Continue with Phase 3 remaining tasks (tests, documentation)

---

**Resolution**: Invalidate IntelliJ caches and reload Maven project

