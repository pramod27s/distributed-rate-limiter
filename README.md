# Distributed Rate Limiter

A production-ready, enterprise-grade distributed rate limiting service built with Spring Boot and Redis.

## Overview
This project provides a highly scalable rate-limiting API. By leveraging Redis as a centralized data store, rate limits are consistently enforced across multiple application instances in a distributed microservices environment. 

## Features
- **Multiple Algorithms Supported:**
  - Token Bucket
  - Fixed Window
  - Sliding Window
  - Leaky Bucket
- **Distributed State:** Backed by Redis to ensure strict consistency across distributed nodes.
- **In-Memory Fallback:** Includes an in-memory rate limiter backend for local testing and zero-dependency setups.
- **Flexible Configuration:** Dynamic rate limiter policies that can be configured on the fly.

## Technologies Used
- Java 21
- Spring Boot
- Redis & Spring Data Redis
- Maven

## Getting Started
1. Ensure you have Java 21 and a running Redis server.
2. Clone the repository.
3. Configure your Redis connection in `src/main/resources/application.properties`.
4. Run the application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

