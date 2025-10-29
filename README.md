# Система бронирования отелей

Сессионный проект по дисциплине "Фреймворк Spring и работа с REST API". 

## Описание проекта

Распределенная система бронирования отелей на базе Spring Boot с использованием Spring Cloud. 
Система включает 4 сервиса:

- **Eureka Server** (порт 8761) - сервис регистрации и обнаружения микросервисов
- **API Gateway** (порт 8080) - маршрутизация запросов и проксирование секрьюрности
- **Hotel Service** (порт 8091) - управление отелями и номерами
- **Booking Service** (порт 8092) - бронирования, регистрация, аутентификация и авторизация

## Предусловия

- Java 21
- Maven (встроен в проект как `./mvnw`)

## Запуск системы

### 1. Eureka Server
cd eureka-server
./mvnw spring-boot:run

### 2. Hotel Service
cd hotel-service
./mvnw spring-boot:run

### 3. Booking Service
cd booking-service
./mvnw spring-boot:run

### 4. API Gateway
cd api-gateway
./mvnw spring-boot:run

## Как пользоваться системой

## Администраторские операции

### Авторизация администратора
curl -X POST http://localhost:8080/api/user/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin"}'

### Создание отеля
curl -X POST http://localhost:8080/api/hotels \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "отель",
    "address": "Иркутск, ул. Северная, 15"
  }'

### Создание номера
curl -X POST http://localhost:8080/api/rooms \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "hotelId": 1,
    "number": "55"
  }'

### 1. Регистрация пользователя
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'

### 2. Авторизация
curl -X POST http://localhost:8080/api/user/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'

### 3. Просмотр отелей
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/hotels

### 4. Просмотр доступных номеров
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/rooms

### 5. Создание бронирования
curl -X POST http://localhost:8080/api/booking \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "startDate": "2025-10-01",
    "endDate": "2025-10-25"
  }'

### 6. Просмотр истории бронирований
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/booking

## Проверка системы

### 1. Проверка Eureka
Откройте http://localhost:8761 - проверьте регистрацию всех поднятых сервисов.

### 2. Проверка Gateway
curl http://localhost:8080/actuator/health

## Тестирование

### Запуск тестов
# Booking Service
cd booking-service && ./mvnw test

# Hotel Service
cd hotel-service && ./mvnw test