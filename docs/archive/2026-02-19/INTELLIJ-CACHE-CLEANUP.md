# IntelliJ IDEA Cache Problem Fix

## Problem
IntelliJ shows errors like:
```
java: Incompatible types: java.lang.Class<de.ruu.lib.jsonb.recursion.ChildrenAdapter> 
cannot be converted to java.lang.Class<? extends jakarta.json.bind.adapter.JsonbAdapter>
```

**But**: The code compiles successfully with Maven!

## Cause
This is a known IntelliJ IDEA issue with:
- JPMS (Java Platform Module System)
- Test module-info.java files
- Cached module information

## Solution

### Option 1: Clear IntelliJ Cache (Recommended)
1. **File → Invalidate Caches...**
2. Select:
   - ✅ Clear file system cache and Local History
   - ✅ Clear VCS Log caches and indexes
   - ✅ Clear downloaded shared indexes
   - ✅ Invalidate and Restart
3. Click **"Invalidate and Restart"**

### Option 2: Reload Maven Projects
1. Open the Maven Tool Window
2. Right-click on the project
3. Select **"Reload All Maven Projects"**

### Option 3: Reload Modules
1. **File → Project Structure → Modules**
2. Delete all modules
3. Click **"+"** → **"Import Module"**
4. Select the root pom.xml

### Option 4: Reopen Project
1. **File → Close Project**
2. Delete project from the list
3. **File → Open** → Select project directory

## Verification
After clearing the cache, the errors should disappear. You can verify that the code is correct with:

```bash
cd /home/r-uu/develop/github/main/root/lib/jsonb
mvn clean test-compile
```

This command should show **BUILD SUCCESS**.

## Why Does Maven Work?
Maven ignores the test module-info.java according to the configuration in pom.xml:

```xml
<testExcludes>
    <testExclude>**/module-info.java</testExclude>
</testExcludes>
```

IntelliJ, however, uses the test module-info.java for JPMS-compliant test execution, which sometimes leads to cache inconsistencies.

## The Code Is Correct!
The `ChildrenAdapter` is correctly implemented:
- Extends `AbstractSetAdapter<Child>`
- `AbstractSetAdapter` implements `JsonbAdapter<Set<T>, JsonValue>`
- Therefore `ChildrenAdapter` indirectly implements `JsonbAdapter<Set<Child>, JsonValue>`
- This is compatible with `@JsonbTypeAdapter(ChildrenAdapter.class)` on a `Set<Child>` field

**→ This is an IntelliJ cache problem, not a code problem!**

