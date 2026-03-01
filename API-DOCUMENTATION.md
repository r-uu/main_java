# API Documentation
**REST API Documentation with OpenAPI/Swagger**
**Last Update:** 2026-02-09
---
## 📚 OpenAPI Documentation
The backend REST API is automatically documented using **MicroProfile OpenAPI**.
### Access API Documentation
**When backend is running:**
1. **OpenAPI UI (Swagger UI):**
   - URL: http://localhost:9080/openapi/ui
   - Interactive API explorer
   - Try out endpoints directly in browser
2. **OpenAPI Specification (YAML):**
   - URL: http://localhost:9080/openapi
   - Machine-readable API specification
   - Can be imported into Postman, Insomnia, etc.
---
## 🚀 Quick Start
### 1. Start Backend
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```
### 2. Open API Documentation
Open browser: http://localhost:9080/openapi/ui
### 3. Try Endpoints
**Example: Get all task groups**
1. Navigate to `/taskgroups` endpoint
2. Click "Try it out"
3. Click "Execute"
4. View response
---
## 🔑 Authentication
Most endpoints require authentication with JWT token.
### Get Token
```bash
curl -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=testuser' \
  -d 'password=testpassword' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend' \
  | jq -r '.access_token'
```
### Use Token in Swagger UI
1. Click "Authorize" button
2. Enter: `Bearer <your-token-here>`
3. Click "Authorize"
4. Now you can use protected endpoints
---
## 📋 Available Endpoints
### Task Groups
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/taskgroups` | List all task groups |
| GET | `/taskgroups/{id}` | Get specific task group |
| POST | `/taskgroups` | Create new task group |
| PUT | `/taskgroups/{id}` | Update task group |
| DELETE | `/taskgroups/{id}` | Delete task group |
### Tasks
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/tasks` | List all tasks |
| GET | `/tasks/{id}` | Get specific task |
| POST | `/tasks` | Create new task |
| PUT | `/tasks/{id}` | Update task |
| DELETE | `/tasks/{id}` | Delete task |
### Health & Monitoring
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Overall health status |
| GET | `/health/live` | Liveness check |
| GET | `/health/ready` | Readiness check |
| GET | `/metrics` | Application metrics |
---
## 📝 API Specification Details
### Info
- **Title:** JEEE-RAAAH backend API
- **Version:** 1.0.0
- **Description:** Backend API with JTA/JPA and OpenAPI on OpenLiberty
### Servers
- **WSL2:** http://172.26.187.214:9080/jeeeraaah
- **Localhost:** http://localhost:9080/jeeeraaah
- **Relative:** /jeeeraaah (uses current host)
### Content Type
- **Request:** `application/json`
- **Response:** `application/json`
- **JSON Provider:** Jackson (replaces default JSON-B/Yasson)
---
## 🔧 Development
### Adding API Documentation to Endpoints
Use OpenAPI annotations in your JAX-RS resources:
```java
@Path("/tasks")
@Tag(name = "Tasks", description = "Task management operations")
public class TaskService {
    @GET
    @Operation(
        summary = "List all tasks",
        description = "Returns a list of all tasks in the system"
    )
    @APIResponse(
        responseCode = "200",
        description = "List of tasks",
        content = @Content(schema = @Schema(implementation = TaskDTO[].class))
    )
    public List<TaskDTO> findAll() {
        // ...
    }
    @POST
    @Operation(summary = "Create new task")
    @APIResponse(
        responseCode = "201",
        description = "Task created",
        content = @Content(schema = @Schema(implementation = TaskDTO.class))
    )
    public TaskDTO create(TaskDTO task) {
        // ...
    }
}
```
### OpenAPI Configuration
Main configuration in: `JeeeRaaah.java`
```java
@OpenAPIDefinition(
    info = @Info(
        title = "JEEE-RAAAH backend API",
        version = "1.0.0",
        description = "Backend API with JTA/JPA"
    ),
    servers = {
        @Server(url = "http://localhost:9080/jeeeraaah")
    }
)
public class JeeeRaaah extends Application {
    // ...
}
```
---
## 📥 Export OpenAPI Specification
### Download YAML
```bash
curl http://localhost:9080/openapi -o openapi.yaml
```
### Download JSON
```bash
curl http://localhost:9080/openapi \
  -H "Accept: application/json" \
  -o openapi.json
```
### Import to Tools
**Postman:**
1. File → Import
2. Select `openapi.yaml`
3. Collection created automatically
**Insomnia:**
1. Create → Import from File
2. Select `openapi.yaml`
3. Endpoints imported
**Visual Studio Code:**
- Install "OpenAPI (Swagger) Editor" extension
- Open `openapi.yaml`
- Preview and edit
---
## 🧪 Testing with cURL
### List Task Groups
```bash
# Without auth (may fail if protected)
curl http://localhost:9080/jeeeraaah/taskgroups
# With JWT token
TOKEN="your-jwt-token-here"
curl http://localhost:9080/jeeeraaah/taskgroups \
  -H "Authorization: Bearer $TOKEN"
```
### Create Task Group
```bash
TOKEN="your-jwt-token-here"
curl -X POST http://localhost:9080/jeeeraaah/taskgroups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Project",
    "description": "My new project"
  }'
```
### Get Specific Task Group
```bash
TOKEN="your-jwt-token-here"
curl http://localhost:9080/jeeeraaah/taskgroups/1 \
  -H "Authorization: Bearer $TOKEN"
```
---
## 📖 Best Practices
### Documentation
✅ **Do:**
- Add `@Operation` summary to all endpoints
- Use `@APIResponse` for different status codes
- Document request/response schemas
- Add examples for complex types
❌ **Don't:**
- Leave endpoints undocumented
- Use generic descriptions like "Get data"
- Forget to document error responses
### Example: Well-Documented Endpoint
```java
@GET
@Path("/{id}")
@Operation(
    summary = "Get task by ID",
    description = "Returns a single task by its unique identifier"
)
@APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Task found",
        content = @Content(schema = @Schema(implementation = TaskDTO.class))
    ),
    @APIResponse(
        responseCode = "404",
        description = "Task not found"
    ),
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized - valid JWT required"
    )
})
public TaskDTO findById(@PathParam("id") Long id) {
    // ...
}
```
---
## 🔍 Troubleshooting
### OpenAPI UI not accessible?
**Check backend is running:**
```bash
curl http://localhost:9080/health
```
**Check OpenAPI endpoint:**
```bash
curl http://localhost:9080/openapi
```
### Endpoints not showing up?
**Verify JAX-RS annotations:**
- Class has `@Path` annotation
- Methods have `@GET`, `@POST`, etc.
- Resource is in correct package (auto-scanned)
**Check server logs:**
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
# Watch console for errors
```
### Authentication issues?
See: [config/JWT-TROUBLESHOOTING.md](config/JWT-TROUBLESHOOTING.md)
---
## 📚 Related Documentation
- [config/CREDENTIALS.md](config/CREDENTIALS.md) - API credentials
- [config/JWT-TROUBLESHOOTING.md](config/JWT-TROUBLESHOOTING.md) - JWT issues
- [MicroProfile OpenAPI Spec](https://microprofile.io/project/eclipse/microprofile-open-api)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
---
## 🎯 Future Improvements
- [ ] Add request/response examples to all endpoints
- [ ] Document all error codes (400, 401, 403, 404, 500)
- [ ] Add schema descriptions for all DTOs
- [ ] Generate API client libraries from OpenAPI spec
- [ ] Add API versioning strategy
- [ ] Set up API mocking for frontend development
---
**Last updated:** 2026-02-09  
**Status:** OpenAPI configured and working  
**Access:** http://localhost:9080/openapi/ui
