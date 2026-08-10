## Quick Start

```bash
git clone git@github.com:ParagonTime/spring-au.git
cd spring-au
```
### Запуск
```bash
./gradlew clean bootJar ; docker-compose up --build
```
### Документация

Регистрация

```text
POST http://localhost:8080/registration
Content-Type: application/json

{
    "login": "user123",
    "password": "pass123",
    "email": "user@mail.com"
}
```

Получение токена
```text
POST http://localhost:8080/api/auth/token
Content-Type: application/json

{"login": "user123", "password": "pass123"}

success 200 OK:
{
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "expiresAt": "2026-04-21T15:30:00Z"
}
```

CRUD users (create при регистрации)
```text
GET http://localhost:8080/users
Authorization: Bearer <token>
```
```text
PUT http://localhost:8080/users
Authorization: Bearer <token>
Content-Type: application/json

{"email": "new@mail.com", "login": "newlogin"}
```

```text
DELETE http://localhost:8080/users
Authorization: Bearer <token>
```

CRUD tasks
```text
POST http://localhost:8080/tasks
Authorization: Bearer <token>
Content-Type: application/json

{"title": "Новая задача", "description": "Описание"}

success 201 Created: TaskDto
```
```text
GET http://localhost:8080/tasks
Authorization: Bearer <token>  (uuid user берется из token)

success 200 OK: Page<TaskDto>
```
```text
GET http://localhost:8080/tasks/{id}
Authorization: Bearer <token>
```
```text
PATCH http://localhost:8080/tasks/{id}/status/{status}
Authorization: Bearer <token>

{status}: CREATED | IN_PROGRESS | DONE

success 200 OK
```
```text
PATCH http://localhost:8080/tasks/{id}/executor/{userId}
Authorization: Bearer <token>

success 200 OK
```
```text
PUT http://localhost:8080/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json

{"title": "Обновлённая задача", "description": "Новое описание"}

success 200 OK: TaskDto
```

```text
DELETE http://localhost:8080/tasks/{id}
Authorization: Bearer <token>

success 204 No Content
```

Search
```text
GET http://localhost:8080/search/users/id?id=<uuid>
Authorization: Bearer <token>

success 200 OK: SearchUserDTO
```
```text
GET http://localhost:8080/search/users/email?email=<email>
Authorization: Bearer <token>

success 200 OK: [SearchUserDTO]
поддерживает нечёткий поиск
```
```text
GET http://localhost:8080/search/tasks/{id}
Authorization: Bearer <token>

success 200 OK: SearchTaskDto
```
0)