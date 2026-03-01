# JEEERAAAH - Getting Started
**Enterprise Task Management System**
**Last Update:** 2026-02-09
---
## 🎯 What is JEEERAAAH?
A Jakarta EE 10 enterprise application for managing tasks and projects with:
- **Backend:** OpenLiberty 25.x with JAX-RS, JPA, CDI
- **Frontend:** JavaFX 25 desktop applications
- **Security:** Keycloak authentication
- **Database:** PostgreSQL 16
- **Reports:** JasperReports
---
## ⚡ Quick Start (3 Steps)
### 1️⃣ Start Docker Environment
```bash
cd /home/r-uu/develop/github/main/config/shared/docker
./startup-and-setup.sh
```
**What this does:**
- Starts PostgreSQL, Keycloak, JasperReports
- Creates databases automatically
- Sets up Keycloak realm
- Waits until all services are healthy (~2-3 minutes)
### 2️⃣ Start Backend
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```
**Backend runs at:** http://localhost:9080
### 3️⃣ Start Frontend
**In IntelliJ IDEA:**
- Run Configuration: `DashAppRunner` or `GanttAppRunner`
- Click ▶️ Run
**Login:**
- Username: `testuser`
- Password: `testpassword`
**Done!** 🎉
---
## 📋 First Time Setup
### Prerequisites
- **Java 25** (GraalVM recommended)
- **Maven 3.9+**
- **Docker** & Docker Compose
- **IntelliJ IDEA** (recommended)
- **WSL2/Ubuntu** (for Windows)
### Clone Project
```bash
cd ~
mkdir -p develop/github
cd develop/github
git clone <repository-url> main
cd main
```
### Configure Shell Aliases
```bash
# Add aliases to .bashrc
cat config/shared/wsl/aliases.sh >> ~/.bashrc
source ~/.bashrc
# Available commands:
ruu-help              # Show all commands
ruu-build             # Build project
ruu-docker-startup    # Start Docker
ruu-docker-status     # Check Docker health
```
### Build Project
```bash
cd /home/r-uu/develop/github/main/root
mvn clean install
```
**Expected:** BUILD SUCCESS after 2-5 minutes
---
## 🔧 Daily Development
### Start Everything
```bash
# 1. Docker (if not running)
ruu-docker-startup
# 2. Backend (in terminal)
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
# 3. Frontend (in IntelliJ)
# Run > DashAppRunner or GanttAppRunner
```
### Stop Everything
```bash
# Stop Backend: Ctrl+C in terminal
# Stop Frontend: Close application window
# Stop Docker: docker compose down
```
---
## 🐛 Troubleshooting
### Docker containers not starting?
```bash
ruu-docker-down
ruu-docker-startup
```
### Backend port 9080 already in use?
```bash
lsof -i :9080
kill -9 <PID>
```
### Authentication fails?
```bash
# Reset Keycloak realm
ruu-keycloak-setup
```
### IntelliJ shows errors but Maven builds?
**Solution:** File → Invalidate Caches → Invalidate and Restart
See: [INTELLIJ-CACHE-CLEANUP.md](INTELLIJ-CACHE-CLEANUP.md)
---
## 📚 Next Steps
### Learn the Applications
**DashApp** - Dashboard application
- Manage task groups
- View task hierarchies
- Edit tasks
**GanttApp** - Gantt chart view
- Visual timeline
- Date filtering
- Task dependencies
### Explore the Code
**Backend:**
- `root/app/jeeeraaah/backend/api/ws_rs` - REST API
- `root/app/jeeeraaah/backend/common/jpa` - Database
**Frontend:**
- `root/app/jeeeraaah/frontend/ui/fx` - JavaFX apps
- `root/app/jeeeraaah/frontend/api_client` - REST client
**Libraries:**
- `root/lib/docker_health` - Health checks
- `root/lib/fx/comp` - JavaFX components
### Documentation
- [QUICK-REFERENCE.md](QUICK-REFERENCE.md) - Command reference
- [PROJECT-STATUS.md](PROJECT-STATUS.md) - Architecture overview
- [config/TROUBLESHOOTING.md](config/TROUBLESHOOTING.md) - Common issues
- [DOCUMENTATION-INDEX.md](DOCUMENTATION-INDEX.md) - All documentation
---
## 🔑 Credentials
See: [config/CREDENTIALS.md](config/CREDENTIALS.md)
**Quick Reference:**
- **Test User:** testuser / testpassword
- **Keycloak Admin:** admin / admin
- **PostgreSQL:** postgres / postgres
---
## ⚙️ Configuration
All configuration is in `config/` directory:
- Docker setup: `config/shared/docker/`
- Scripts: `config/shared/scripts/`
- Documentation: `config/*.md`
**Important files:**
- `docker-compose.yml` - Container configuration
- `application.properties` - Backend configuration
- `beans.xml` - CDI configuration
---
## 🧪 Testing
```bash
# Run all tests
cd /home/r-uu/develop/github/main/root
mvn test
# Run specific module tests
cd root/app/jeeeraaah/backend/api/ws_rs
mvn test
```
---
## 🚀 Advanced Topics
### JPMS (Java Platform Module System)
This project uses Java modules. See:
- [JPMS-INTELLIJ-QUICKSTART.md](JPMS-INTELLIJ-QUICKSTART.md)
- [JPMS-RUN-CONFIGURATIONS.md](JPMS-RUN-CONFIGURATIONS.md)
### Docker Health Checks
Automatic health monitoring and fixes:
- `root/lib/docker_health` - Health check library
- Monitors: PostgreSQL, Keycloak, JasperReports
- Auto-fix: Starts stopped containers
### MapStruct
Bean mapping framework:
- `root/app/jeeeraaah/common/api/mapping` - Mappers
- `root/app/jeeeraaah/frontend/common/mapping` - UI mappers
---
## 🆘 Need Help?
1. **Check documentation:** [DOCUMENTATION-INDEX.md](DOCUMENTATION-INDEX.md)
2. **Common issues:** [config/TROUBLESHOOTING.md](config/TROUBLESHOOTING.md)
3. **Project status:** [PROJECT-STATUS.md](PROJECT-STATUS.md)
4. **Improvements:** [PROJECT-IMPROVEMENTS.md](PROJECT-IMPROVEMENTS.md)
---
**Happy Coding!** 🎉
*Last updated: 2026-02-09*
