
# Railway Reservation System
## High-Level System Architecture

### 1. Project Overview
The Railway Reservation System is developed using a microservices architecture. Each service handles a separate business function.

### 2. Technology Stack
- Java 17
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Netflix Eureka Server
- MySQL
- JWT Authentication
- RabbitMQ
- Maven

### 3. Microservices and Ports

| Service | Port |
|---|---:|
| API Gateway | 8080 |
| Registration Service | 8081 |
| Login Service | 8082 |
| Train Service | 8083 |
| Reservation Service | 8084 |
| Payment Service | 8085 |
| Messaging Service | 8086 |
| Eureka Server | 8761 |

### 4. Architecture
The API Gateway acts as the entry point for client requests. Eureka Server handles service discovery. Each microservice manages its respective business functionality and database operations.

### 5. Communication
- Client requests are routed through the API Gateway.
- Microservices register with Eureka Server.
- JWT is used for authentication and authorization.
- RabbitMQ supports asynchronous notification messaging.
- MySQL stores service-specific application data.
