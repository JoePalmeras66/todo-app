# Todo App

A RESTful Todo application built with Spring Boot, Spring Integration, and PostgreSQL.

## Features

- Full CRUD operations for todos
- Priority levels (LOW, MEDIUM, HIGH)
- Filter by completion status or priority
- Search todos by title
- Toggle completion status
- Event-driven architecture with Spring Integration
- Docker support

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Integration**
- **PostgreSQL**
- **Lombok**
- **JUnit 5 + Mockito**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/todos` | Get all todos |
| GET | `/api/todos/{id}` | Get todo by ID |
| POST | `/api/todos` | Create new todo |
| PUT | `/api/todos/{id}` | Update todo |
| DELETE | `/api/todos/{id}` | Delete todo |
| GET | `/api/todos/status/{completed}` | Filter by completion status |
| GET | `/api/todos/priority/{priority}` | Filter by priority |
| GET | `/api/todos/search?title=` | Search by title |
| PATCH | `/api/todos/{id}/toggle` | Toggle completion |

## Getting Started

### Prerequisites

- Docker and Docker Compose

### Running with Docker

```bash
docker-compose up
```

The application will be available at `http://localhost:8080`.

### Running Locally

Requirements: JDK 17, PostgreSQL

1. Start PostgreSQL on port 5432
2. Create database `tododb`
3. Run the application:

```bash
./gradlew bootRun
```

## Example Requests

### Create a Todo

```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot",
    "description": "Complete the tutorial",
    "priority": "HIGH"
  }'
```

### Get All Todos

```bash
curl http://localhost:8080/api/todos
```

### Toggle Completion

```bash
curl -X PATCH http://localhost:8080/api/todos/1/toggle
```

## Project Structure

```
src/main/java/com/example/todoapp/
├── TodoAppApplication.java
├── config/
│   └── IntegrationConfig.java
├── controller/
│   └── TodoController.java
├── entity/
│   └── Todo.java
├── exception/
│   └── GlobalExceptionHandler.java
├── integration/
│   └── TodoEventGateway.java
├── repository/
│   └── TodoRepository.java
└── service/
    ├── TodoService.java
    └── TodoServiceImpl.java
```

## License

MIT
