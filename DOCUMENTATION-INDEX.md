# Project Documentation - Index
**📌 Main Entry Point: [README.md](README.md)**
**Last Updated:** 2026-02-10
---
## 🚀 Quick Start
| Document | Description |
|----------|-------------|
| [README.md](README.md) | Main documentation |
| [GETTING-STARTED.md](GETTING-STARTED.md) | ⭐ **Complete getting started guide** (consolidated) |
| [QUICK-REFERENCE.md](QUICK-REFERENCE.md) | Command quick reference |
| [QUICK-STATUS.md](QUICK-STATUS.md) | Current project status at a glance |
| [SCRIPTS-OVERVIEW.md](SCRIPTS-OVERVIEW.md) | Overview of all scripts & aliases |
---
## 📝 Project Management
| Document | Description |
|----------|-------------|
| [PROJECT-STATUS.md](PROJECT-STATUS.md) | Current project status |
| [PROJECT-IMPROVEMENTS.md](PROJECT-IMPROVEMENTS.md) | ⭐ Comprehensive improvement recommendations |
| [IMPROVEMENT-PRIORITIES.md](IMPROVEMENT-PRIORITIES.md) | ⭐ **Detailed improvement priorities** |
| [FINAL-SUMMARY.md](FINAL-SUMMARY.md) | Cleanup completion summary |
| [DEPRECATED-FILES.md](DEPRECATED-FILES.md) | Archived/deprecated files status |
| [DEPRECATED-CLEANUP-FINAL.md](DEPRECATED-CLEANUP-FINAL.md) | Final deprecation cleanup report |
| [todo.md](todo.md) | Task list and priorities |
---
## 🔑 Security & Credentials
| Document | Description |
|----------|-------------|
| [config/CREDENTIALS.md](config/CREDENTIALS.md) | ⭐ **Complete credentials reference** (consolidated) |
| [config/KEYCLOAK-ADMIN-CONSOLE.md](config/KEYCLOAK-ADMIN-CONSOLE.md) | Keycloak admin console guide |
| [config/JWT-TROUBLESHOOTING.md](config/JWT-TROUBLESHOOTING.md) | JWT troubleshooting |
---
## 📡 API Documentation
| Document | Description |
|----------|-------------|
| [API-DOCUMENTATION.md](API-DOCUMENTATION.md) | ⭐ **REST API documentation guide** (OpenAPI/Swagger) |
| http://localhost:9080/openapi/ui | Interactive API explorer (when backend running) |
| http://localhost:9080/openapi | OpenAPI specification (YAML/JSON) |
---
## 🐳 Docker & Infrastructure
| Document | Description |
|----------|-------------|
| [config/shared/docker/LIB-TEST-FIX.md](config/shared/docker/LIB-TEST-FIX.md) | Why lib_test database works automatically |
| [config/shared/docker/MULTI-DB-SOLUTION.md](config/shared/docker/MULTI-DB-SOLUTION.md) | Multi-database setup |
| [config/shared/docker/initdb/README.md](config/shared/docker/initdb/README.md) | Database initialization |
---
## 🏗️ Architecture & Development
### General
| Document | Description |
|----------|-------------|
| [root/app/jeeeraaah/doc/md/architecture/requirements.md](root/app/jeeeraaah/doc/md/architecture/requirements.md) | Requirements |
### Backend
| Document | Description |
|----------|-------------|
| [root/app/jeeeraaah/backend/api/ws.rs/README.md](root/app/jeeeraaah/backend/api/ws.rs/README.md) | Backend REST API documentation |
### Frontend
| Document | Description |
|----------|-------------|
| [root/lib/fx/comp/readme.md](root/lib/fx/comp/readme.md) | JavaFX Component Framework |
| [root/lib/fx/comp/doc/fx-comp-architecture.md](root/lib/fx/comp/doc/fx-comp-architecture.md) | FX Component Architecture |
### Libraries
| Document | Description |
|----------|-------------|
| [root/lib/docker.health/README.md](root/lib/docker.health/README.md) | Docker Health Check Library |
| [root/lib/mp.config/README.md](root/lib/mp.config/README.md) | MicroProfile Config Integration |
---
## 📝 Data Model
| Document | Description |
|----------|-------------|
| [root/app/jeeeraaah/doc/md/datamodel/datamodel.md](root/app/jeeeraaah/doc/md/datamodel/datamodel.md) | Base data model |
| [root/app/jeeeraaah/doc/md/datamodel/datamodel-extended.md](root/app/jeeeraaah/doc/md/datamodel/datamodel-extended.md) | Extended data model |
---
## 🔧 Build & Configuration
| Document | Description |
|----------|-------------|
| [bom/readme.md](bom/readme.md) | Bill of Materials (BOM) |
| [config/CONFIGURATION-GUIDE.md](config/CONFIGURATION-GUIDE.md) | Configuration guide |
| [config/SINGLE-POINT-OF-TRUTH.md](config/SINGLE-POINT-OF-TRUTH.md) | Configuration single point of truth |
| [config/STRUCTURE.md](config/STRUCTURE.md) | Project structure |
| [config/QUICK-COMMANDS.md](config/QUICK-COMMANDS.md) | Frequently used commands |
---
## 🔨 IntelliJ IDEA & Development Tools
| Document | Description |
|----------|-------------|
| [INTELLIJ-CACHE-CLEANUP.md](INTELLIJ-CACHE-CLEANUP.md) | ⭐ Fix IntelliJ JPMS cache issues |
| [JPMS-INTELLIJ-QUICKSTART.md](JPMS-INTELLIJ-QUICKSTART.md) | JPMS setup in IntelliJ |
| [JPMS-RUN-CONFIGURATIONS.md](JPMS-RUN-CONFIGURATIONS.md) | JPMS run configurations |
| [config/INTELLIJ-APPLICATION-RUN-CONFIG.md](config/INTELLIJ-APPLICATION-RUN-CONFIG.md) | IntelliJ application run configs |
| [config/FRESH-CLONE-SETUP.md](config/FRESH-CLONE-SETUP.md) | Setup from fresh clone |
---
## 🐛 Troubleshooting
| Document | Description |
|----------|-------------|
| [config/TROUBLESHOOTING.md](config/TROUBLESHOOTING.md) | ⭐ General troubleshooting |
| [config/JWT-TROUBLESHOOTING.md](config/JWT-TROUBLESHOOTING.md) | JWT/Authentication issues |
---
## 📚 Archive
**Archived documentation** in `config/archive/`:
- `docs-20260123/` - Initial archive (7 files)
- `docs-20260209/` - Main cleanup (29+ files)
- `docs-20260209-final/` - Final cleanup (7 files)
**Consolidated originals:**
- QUICKSTART.md, GETTING-STARTED-old.md, STARTUP-QUICK-GUIDE.md → GETTING-STARTED.md
- AUTHENTICATION-CREDENTIALS.md, CREDENTIALS-OVERVIEW.md, CREDENTIALS.md → config/CREDENTIALS.md
See [DEPRECATED-FILES.md](DEPRECATED-FILES.md) for details.
---
## 📖 Reading Order for New Developers
1. [README.md](README.md) - Start here
2. [GETTING-STARTED.md](GETTING-STARTED.md) ⭐ - Complete guide
3. [config/CREDENTIALS.md](config/CREDENTIALS.md) ⭐ - All credentials
4. [API-DOCUMENTATION.md](API-DOCUMENTATION.md) ⭐ - REST API guide
5. [INTELLIJ-CACHE-CLEANUP.md](INTELLIJ-CACHE-CLEANUP.md) - Fix IntelliJ issues
6. [PROJECT-STATUS.md](PROJECT-STATUS.md) - Architecture
7. [IMPROVEMENT-PRIORITIES.md](IMPROVEMENT-PRIORITIES.md) ⭐ - Where to improve
8. [todo.md](todo.md) - Tasks
---
## 🎯 Essential Documentation
⭐ **Start with these:**
- [GETTING-STARTED.md](GETTING-STARTED.md) - Complete guide (consolidated)
- [config/CREDENTIALS.md](config/CREDENTIALS.md) - All credentials (consolidated)
- [API-DOCUMENTATION.md](API-DOCUMENTATION.md) - REST API guide (new)
- [IMPROVEMENT-PRIORITIES.md](IMPROVEMENT-PRIORITIES.md) - Improvement roadmap (new)
---
**Need help?** [QUICK-STATUS.md](QUICK-STATUS.md) | [config/TROUBLESHOOTING.md](config/TROUBLESHOOTING.md)
