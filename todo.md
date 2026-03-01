# Project TODO List

**Last Updated:** 2026-03-01  
**Status:** Build successful, Documentation consolidated

---

## ✅ Recently Completed (2026-03-01)

### Documentation Consolidation 🎯
- ✅ **MAJOR:** Consolidated 40 root MD files → 11 files (82% reduction!)
- ✅ Created JPMS-REFERENCE.md (consolidated 7 JPMS docs)
- ✅ Archived 15 historical documents (reports + fixes → docs/archive/)
- ✅ Deleted 10 duplicate/obsolete documentation files
- ✅ Updated DOCUMENTATION-INDEX.md to reflect current state
- ✅ Created DOCUMENTATION-CONSOLIDATION-PLAN.md

### Scripts & Aliases
- ✅ Removed 8 obsolete conversion scripts (Hamcrest/AssertJ migration completed)
- ✅ Added 3 Liberty aliases: ruu-liberty-start, ruu-liberty-run, ruu-liberty-stop
- ✅ Updated alias help output to show backend/frontend commands

### Publication Preparation
- ✅ Audited "JPMS in Action - jeeeraaah" document (⚠️ needs completion)
- ✅ Audited "Modular Software in Java" document (✅ complete)

### VS Code & IDE
- ✅ Documented VS Code error vs Maven build discrepancy (VSCODE-ERRORS-EXPLAINED-2026-03-01.md)
- ✅ Clarified WAR module intentionally has no JPMS (backend.api.ws.rs)
- ✅ Updated .vscode/settings.json to suppress classpath warnings

### Code Quality
- ✅ Consolidated multi-line log statements using text blocks in:
  - lib.util (ClasspathTest.java - 3 statements)
  - lib.jsonb (TestJsonBWithMaxEncapsulationForData.java - 2 statements)
  - frontend.ui.fx.test (DataFactory.java - simplified)

---

## 🔥 Priority 1 - Publication Preparation

### Complete JPMS in Action Document
- [ ] **CRITICAL:** Complete Keycloak chapter in JPMS in Action document  
  → Document ends abruptly at "The Server Side"
  → File: [root/app/jeeeraaah/doc/md/jpms in action - jeeeraaah/jpms in action - jeeeraaah.md](root/app/jeeeraaah/doc/md/jpms in action - jeeeraaah/jpms in action - jeeeraaah.md)

### Verify Publication Documents
- [ ] Check all images in JPMS in Action (SVG/PNG files present?)
- [ ] Check all images in Modular Software in Java
- [ ] Decide: English translation needed for publication?

---

## 📋 Priority 2 - Development

---

## 📋 Priority 2 - Important (This Week)

### Code Quality
- [x] Consolidate multi-line log statements using text blocks (`"""`) - **✅ COMPLETED 2026-03-01**
- [x] Remove recursion guard in TaskTreeTableController if not needed - **✅ KEPT: Necessary for circular refs**
- [ ] Fix remaining compiler warnings in DashController.java
- [ ] Review and fix unused parameter warnings

### Documentation
- [ ] Consolidate startup guides (QUICKSTART.md + GETTING-STARTED.md + STARTUP-QUICK-GUIDE.md)
- [ ] Merge credentials documentation (3 files → 1)
- [x] Update DOCUMENTATION-INDEX.md with current structure - **✅ Updated**
- [ ] Translate remaining German documentation (readme.de.md files)

### Infrastructure
- [ ] Check why there's no Dockerfile for JasperReports
- [ ] Verify JasperReports is activated in docker-compose.yml
- [ ] Test Docker environment health checks

---

## 🚀 Priority 3 - Recommended (This Month)

### Testing
- [ ] Add unit tests for task hierarchy edge cases
- [ ] Implement integration tests for REST API
- [ ] Add JaCoCo for code coverage measurement
- [ ] Target: 70%+ test coverage

### CI/CD
- [ ] Set up GitHub Actions or GitLab CI pipeline
- [ ] Add automated build on push/pull request
- [ ] Add automated tests in CI
- [ ] Add dependency vulnerability scanning (OWASP, Snyk)

### Architecture
- [ ] Implement ArchUnit tests for layer boundaries
- [ ] Enforce lib/* must not depend on app/*
- [ ] Add architecture documentation
- [ ] Review and document module dependencies

### Code Quality Tools
- [ ] Add SpotBugs plugin to Maven
- [ ] Add PMD plugin to Maven
- [ ] Add Checkstyle configuration
- [ ] Configure SonarQube (optional)

---

## 💡 Priority 4 - Nice to Have (Next Quarter)

### Monitoring & Observability
- [ ] Add Micrometer for application metrics
- [ ] Set up Prometheus endpoint
- [ ] Add structured logging (JSON format)
- [ ] Implement distributed tracing (OpenTelemetry)

### Security
- [ ] Add security headers to REST API
- [ ] Implement comprehensive input validation
- [ ] Review credential management (move to env vars/secrets)
- [ ] Add OWASP dependency check to build

### Performance
- [ ] Profile application startup time
- [ ] Optimize Docker image sizes
- [ ] Add caching where appropriate
- [ ] Review database query performance

### Documentation
- [ ] Generate JavaDoc for all modules
- [ ] Create architecture decision records (ADRs)
- [ ] Add API documentation (OpenAPI/Swagger)
- [ ] Create developer onboarding guide

---

## 📝 Ongoing Tasks

### Maintenance
- [ ] Keep dependencies up to date
- [ ] Review and archive obsolete documentation regularly
- [ ] Update PROJECT-STATUS.md periodically
- [ ] Respond to security advisories

### Best Practices
- [ ] Use text blocks for multi-line strings
- [ ] Prefer method references over lambdas
- [ ] Keep methods focused and small
- [ ] Document public APIs

---

## 🎯 Long-term Goals

- Achieve 80%+ test coverage
- Fully automated CI/CD pipeline
- Comprehensive monitoring and alerting
- Zero critical security vulnerabilities
- Clean, maintainable codebase
- Complete English documentation
- Active community contributions

---

## 📚 Reference Documents

- **PROJECT-IMPROVEMENTS.md** - Detailed improvement recommendations
- **FINAL-SUMMARY.md** - Cleanup completion summary
- **BEREINIGUNG-ABSCHLUSS.md** - German cleanup summary
- **PROJECT-CLEANUP-2026-02-09.md** - Detailed cleanup log

---

**Next Review:** After completing Priority 1 tasks
