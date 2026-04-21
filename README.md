## Quick Start

```bash
# 1. Клонировать репозиторий
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
POST http://localhost:8081/registration
Content-Type: application/json

{"login": "user123", "password": "pass123"}

success 201 Created (пустое тело)

error 409 Conflict: {"message": "Логин уже используется user123"}
error 400 Bad Request: {"message": "Логин не может быть пустым"}
```

Получение токена
```text
POST http://localhost:8081/api/auth/token
Content-Type: application/json

{"login": "user123", "password": "pass123"}

success 200 OK:
{
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "expiresAt": "2026-04-21T15:30:00Z"
}

error 401 Unauthorized: {"message": "Неверный логин или пароль"}
error 400 Bad Request: {"message": "Логин не может быть пустым"}
```

Публичный ключ (JWKS)
```text
GET http://localhost:8081/oauth2/jwks

success 200 OK:
{
    "keys": [{
        "kty": "RSA",
        "e": "AQAB",
        "kid": "d52d0270-cc16-4192-bf9d-e2771128687f",
        "n": "qMPIJjw7ndxDQjEi..."
    }]
}
```
Проверка работоспособности
```text
GET http://localhost:8081/actuator/health

{
    "groups": [
        "liveness",
        "readiness"
    ],
    "status": "UP"
}
```
