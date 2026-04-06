# 🛡️ OSINT Dashboard — Backend Engine (CIRT Edition)

Backend API and search engine for the OSINT Intelligence Platform used for analyzing leaked data and supporting incident response operations.

This backend service provides secure authentication, high-performance search, caching, and scalable data ingestion capabilities designed for operational environments such as SOC / CIRT.

---

# 👨‍💻 Author

**Tchuente Kenmegne Joel Parfait**
Backend Developer | OSINT Analyst
Centre de Réponse aux Incidents Informatiques (CIRT) — ANTIC

---

# 📌 Overview

The **OSINT Backend Engine** is a Spring Boot-based API responsible for:

* Processing search requests
* Managing authentication
* Handling leaked data queries
* Managing caching via Redis
* Providing REST API endpoints
* Supporting large-scale data ingestion
* Ensuring system stability and performance

This backend has been **analyzed, stabilized, and optimized** following deployment issues discovered during system initialization.

---

# 🚀 Key Improvements & Critical Fixes

The following production-critical fixes were implemented to ensure system reliability and operational readiness.

---

## 1. Security & CORS Configuration Fix

Problem:

```text
401 Unauthorized errors between frontend and backend
```

Root Cause:

```text
Default Spring Security configuration blocking cross-origin requests
```

Solution:

Custom Security configuration implemented.

File:

```text
SecurityConfig.java
```

Result:

```text
Frontend (port 3000) successfully communicates with backend (port 8080)
```

---

## 2. MongoDB Full-Text Index Activation

Problem:

```text
Search by name causing HTTP 500 errors
```

Root Cause:

```text
Missing MongoDB text index
```

Solution:

Manual creation of MongoDB text index.

Command:

```javascript
db.leakeddata.createIndex({ "name": "text" })
```

Result:

```text
Stable full-text search functionality
```

---

## 3. Redis Cache Stability

Problem:

```text
Backend failure when Redis service stopped
```

Root Cause:

```text
Hard dependency on Redis cache
```

Solution:

Implemented resilience logic in service layer.

Result:

```text
Backend continues operating even if Redis is unavailable
```

---

## 4. Data Mapping Correction

Problem:

```text
Null values in search results
```

Root Cause:

```text
Mismatch between MongoDB fields and Java model
```

Example:

```json
firstname
lastname
```

Corrected To:

```json
name
```

Result:

```text
Consistent and reliable data mapping
```

---

# 🧱 System Architecture

```text
Frontend (React)
        ↓
Spring Boot API
        ↓
Redis Cache
        ↓
MongoDB Database
```

Future architecture:

```text
Frontend
        ↓
Spring Boot API
        ↓
Elasticsearch
        ↓
MongoDB
```

---

# ⚙️ Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring Security
* REST API

## Database

* MongoDB

## Cache

* Redis

## Tools

* Maven
* Postman
* Git
* VS Code

---

# 📋 Prerequisites

Ensure the following services are installed.

---

## Java

```bash
java -version
```

Required:

```text
Java 17+
```

---

## Maven

```bash
mvn -version
```

Required:

```text
Maven 3.6+
```

---

## MongoDB

```bash
mongod --version
```

Required:

```text
MongoDB 5.0+
```

---

## Redis

```bash
redis-server --version
```

Recommended:

```text
Redis 6+
```

---

# 🔧 Database Setup (MongoDB)

---

## Start MongoDB

Linux (Kali):

```bash
sudo systemctl start mongodb
sudo systemctl enable mongodb
```

---

## Create Database User

Open MongoDB shell:

```bash
mongosh
```

Run:

```javascript
use admin

db.createUser({
  user: "admin",
  pwd: "admin123",
  roles: [
    {
      role: "readWrite",
      db: "leaks_db"
    }
  ]
})
```

---

## Import Data

Supports large datasets.

```bash
mongoimport \
--db leaks_db \
--collection leakeddata \
--file leaks_data.json \
--type json
```

---

## Create Critical Index

Mandatory for search stability.

```javascript
use leaks_db

db.leakeddata.createIndex({
  name: "text"
})
```

---

# ⚙️ Backend Configuration

Edit:

```text
src/main/resources/application.properties
```

---

## MongoDB Connection

```properties
spring.data.mongodb.uri=mongodb://admin:admin123@127.0.0.1:27017/leaks_db?authSource=admin
```

---

## Redis Configuration

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

# 🚀 Run Backend Server

Build project:

```bash
mvn clean install -DskipTests
```

Start server:

```bash
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

---

# 📊 API Endpoints

---

## Authentication

```text
POST /auth/login
POST /auth/logout
POST /auth/change-password
```

---

## Search

```text
GET /search/name
GET /search/email
GET /search/phone
GET /search/address
```

Example:

```bash
curl "http://localhost:8080/search/name?value=john"
```

---

## Cache Management

```text
GET /api/cache/stats
GET /api/cache/health
POST /api/cache/clear
```

---

## Health Check

```text
GET /search/health
```

---

# 🧪 Health Checks

---

## Check API

```bash
curl http://localhost:8080/search/health
```

---

## Check Redis

```bash
redis-cli ping
```

Expected:

```text
PONG
```

---

## Check Database

```javascript
db.leakeddata.countDocuments()
```

---

# 📁 Project Structure

```text
backend/
│
├── src/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── config/
│   └── security/
│
├── resources/
│   └── application.properties
│
├── pom.xml
└── README_BACKEND.md
```

---

# 🔐 Security Notes

Never expose MongoDB publicly.

Do NOT open:

```text
Port 27017
```

Without:

* authentication
* firewall rules
* TLS encryption

---

# 🐛 Troubleshooting

---

## Error 401 Unauthorized

Check:

```text
SecurityConfig.java
```

Ensure:

```text
requestMatchers("/search/**").permitAll()
```

---

## Error 500 on Search

Cause:

```text
Missing MongoDB index
```

Fix:

```javascript
db.leakeddata.createIndex({
  name: "text"
})
```

---

## Redis Connection Failure

Check:

```bash
redis-cli ping
```

---

## Port 8080 Already in Use

Linux:

```bash
lsof -i :8080
kill -9 <PID>
```

---

# 📊 Performance Notes

Current system supports:

```text
1000+ records
Fast search response
Redis caching
Stable API
```

Planned support:

```text
Millions of records
Elasticsearch indexing
Distributed search
High availability
```

---

# 📌 Future Improvements

* Elasticsearch integration
* Fuzzy search
* Autocomplete
* Pagination
* Advanced filtering
* Logging system
* Audit tracking
* Role-based access control
* Docker deployment
* Monitoring (Prometheus / Grafana)

---

# 📄 License

Internal research and cybersecurity project.

---

# 🙏 Acknowledgments

Centre de Réponse aux Incidents Informatiques (CIRT)
Agence Nationale des Technologies de l’Information et de la Communication (ANTIC)

---

**Maintained by:**
Tchuente Kenmegne Joel Parfait
