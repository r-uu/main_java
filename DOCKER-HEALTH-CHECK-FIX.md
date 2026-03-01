# Docker Health Check Fix - PostgreSQL JDBC Driver Loading

**Date:** 2026-03-01  
**Issue:** DashAppRunner/GanttAppRunner failed to start with "Docker environment not available" error  
**Root Cause:** PostgreSQL JDBC driver not loaded in JPMS module-path at runtime

## Problem Description

When starting DashAppRunner or GanttAppRunner, the Docker health check failed with:

```
❌ Cannot connect to database 'jeeeraaah': No suitable driver found for jdbc:postgresql://localhost:5432/jeeeraaah
❌ Cannot connect to database 'lib_test': No suitable driver found for jdbc:postgresql://localhost:5432/lib_test
❌ Cannot connect to database 'keycloak': No suitable driver found for jdbc:postgresql://localhost:5432/keycloak
```

Docker containers were healthy and running, but the JDBC driver was not available at runtime.

## Root Cause Analysis

The PostgreSQL JDBC driver (`org.postgresql:postgresql`) is an **automatic JPMS module** named `org.postgresql.jdbc`.

- ✅ Dependency was declared in `docker_health/pom.xml`
- ❌ Missing `requires org.postgresql.jdbc;` in module-info.java files
- ❌ JPMS requires explicit module requirements for runtime dependencies

With JPMS (Java Platform Module System), compile-time dependencies ≠ runtime module availability. The driver needs to be both:
1. On the Maven classpath (via pom.xml dependency)
2. Required in module-info.java for JPMS module resolution

## Solution

### 1. Added JDBC Driver Requirement to docker_health Module

**File:** `root/lib/docker_health/src/main/java/module-info.java`

```java
module de.ruu.lib.docker.health
{
	requires static lombok;
	requires org.slf4j;
	requires java.sql;
	requires org.postgresql.jdbc;  // ← ADDED: PostgreSQL JDBC driver (automatic module)
	requires de.ruu.lib.util.config.mp;
	requires static de.ruu.lib.keycloak.admin;

	exports de.ruu.lib.docker.health;
	exports de.ruu.lib.docker.health.check;
	exports de.ruu.lib.docker.health.fix;
}
```

### 2. Added JDBC Driver Requirement to ui.fx Module

**File:** `root/app/jeeeraaah/frontend/ui/fx/src/main/java/module-info.java`

```java
module de.ruu.app.jeeeraaah.frontend.ui.fx
{
	// ... other requires ...
	requires de.ruu.lib.docker.health;
	requires de.ruu.lib.keycloak.admin;
	requires org.postgresql.jdbc;  // ← ADDED: Required by docker.health JDBC health checks
	// ... rest of module declaration ...
}
```

### 3. Added Explicit Dependency to ui.fx POM

**File:** `root/app/jeeeraaah/frontend/ui/fx/pom.xml`

```xml
<dependency>
	<groupId>r-uu</groupId>
	<artifactId>r-uu.lib.docker_health</artifactId>
	<version>${project.version}</version>
	<scope>compile</scope>
</dependency>
<!-- ↓ ADDED: PostgreSQL JDBC driver - required by docker_health runtime -->
<dependency>
	<groupId>org.postgresql</groupId>
	<artifactId>postgresql</artifactId>
</dependency>
<dependency>
	<groupId>r-uu</groupId>
	<artifactId>r-uu.lib.keycloak_admin</artifactId>
	<version>${project.version}</version>
	<scope>compile</scope>
</dependency>
```

## Verification

After the fix, both applications start successfully:

```
✅ Docker daemon is running
✅ Database 'jeeeraaah' is accessible
✅ Database 'lib_test' is accessible
✅ Database 'keycloak' is accessible
✅ Keycloak server is running
✅ Keycloak realm 'jeeeraaah-realm' is fully configured
✅ JasperReports service is running
✅ ALL SERVICES HEALTHY - Ready to start!
✅ Docker environment health check passed
```

## Testing

```bash
# Test DashAppRunner
cd root/app/jeeeraaah/frontend/ui/fx
mvn exec:java

# Test GanttAppRunner
mvn exec:java@gantt
```

## Key Learnings

1. **JPMS Automatic Modules:** PostgreSQL JDBC driver is an automatic module (`org.postgresql.jdbc`)
   - Check module name: `jar --describe-module --file postgresql-*.jar`
   
2. **Transitive Dependencies:** Even if a dependency is transitive (docker_health → postgresql), consuming modules must explicitly `requires` it if they use exec-maven-plugin

3. **Module vs Classpath:** In JPMS, dependencies on classpath but not in module-path will not be available at runtime

4. **ServiceLoader Pattern:** JDBC drivers use ServiceLoader, but JPMS still requires the module to be on module-path

## Related Issues

- Initial issue: VS Code Java Language Server errors (resolved via cache cleanup)
- Module naming convention change: `ws.rs` → `ws_rs`, `api.client` → `api_client` (completed)
- IDE configuration updates (completed)

## Status

✅ **RESOLVED** - Both DashAppRunner and GanttAppRunner start successfully
✅ All Docker health checks pass
✅ All database connections working
✅ Application proceeds to authentication and UI initialization
