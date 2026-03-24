<div align="center">
  <img src="https://via.placeholder.com/120/3b82f6/ffffff?text=Sutra" width="120"/>

  # Sutra AI – Backend

  ### ⚙️ RESTful API for Intelligent AI Assistant

  <p>
    <img src="https://img.shields.io/github/stars/Mahesh1354/sutra-ai-backend?style=social" />
    <img src="https://img.shields.io/github/forks/Mahesh1354/sutra-ai-backend?style=social" />
    <img src="https://img.shields.io/github/license/Mahesh1354/sutra-ai-backend" />
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-17-blue?logo=openjdk" />
    <img src="https://img.shields.io/badge/SpringBoot-3.2-green?logo=springboot" />
    <img src="https://img.shields.io/badge/MySQL-8-blue?logo=mysql" />
    <img src="https://img.shields.io/badge/JWT-Auth-black?logo=jsonwebtokens" />
    <img src="https://img.shields.io/badge/Status-Active-success" />
  </p>
</div>

---

## 🚀 Overview

The **Sutra AI Backend** is a scalable RESTful API built with **Spring Boot** that powers an AI chat application.  
It provides secure authentication, conversation management, and real-time AI response streaming using **Google Gemini AI**.

---

## ✨ Key Features

- 🔐 **JWT Authentication & Authorization**
- 🤖 **Gemini AI Integration (Streaming + Non-streaming)**
- 💾 **MySQL Database with JPA/Hibernate**
- 💬 **Conversation & Message Management (CRUD)**
- ⚡ **Real-time Streaming Responses**
- 🛡️ **Validation & Security Configurations**
- 🌐 **CORS Handling**
- 📊 **Rate Limiting & Logging**

---

## 🛠️ Tech Stack

| Category | Technology |
|--------|-----------|
| Backend | Spring Boot 3.2 |
| Security | Spring Security + JWT |
| Database | MySQL 8 |
| ORM | Spring Data JPA (Hibernate) |
| Build Tool | Maven |
| AI Integration | Google Gemini API |

---

## 📂 Project Structure

```

sutra-ai-backend/
│
├── controller/
├── service/
├── repository/
├── model/
├── dto/
├── security/
├── config/
│
└── resources/
├── application.properties

````

---

## ⚙️ Setup & Installation

### 1️⃣ Clone Repository

```bash
git clone https://github.com/Mahesh1354/sutra-ai-backend.git
cd sutra-ai-backend
````

---

### 2️⃣ Database Setup

```sql
CREATE DATABASE sutra_ai;

CREATE USER 'sutra_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON sutra_ai.* TO 'sutra_user'@'localhost';
FLUSH PRIVILEGES;
```

---

### 3️⃣ Configure Application

Copy and update:

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Edit:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/sutra_ai
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Gemini
gemini.api.key=your-api-key
gemini.model=gemini-2.0-flash-exp
```

---

### 4️⃣ Run Application

```bash
mvn clean install
mvn spring-boot:run
```

👉 API runs at: **[http://localhost:8080](http://localhost:8080)**

---

## 📡 API Endpoints

### 🔐 Authentication

| Method | Endpoint           | Description    |
| ------ | ------------------ | -------------- |
| POST   | /api/auth/register | Register user  |
| POST   | /api/auth/login    | Login          |
| GET    | /api/auth/validate | Validate token |

---

### 💬 Chat

| Method | Endpoint                         | Description         |
| ------ | -------------------------------- | ------------------- |
| POST   | /api/chat/send                   | Send message        |
| POST   | /api/chat/stream                 | Stream response     |
| GET    | /api/chat/conversations          | Get conversations   |
| DELETE | /api/chat/conversations/{id}     | Delete conversation |
| PATCH  | /api/chat/conversations/{id}     | Rename              |
| PATCH  | /api/chat/conversations/{id}/pin | Pin/unpin           |
| PUT    | /api/chat/messages/{id}          | Edit message        |
| DELETE | /api/chat/messages/{id}          | Delete message      |

---

## 🔐 Authentication Flow

```
Client → Login → Server → JWT Generated → Client stores token  
Client → Sends token → Server validates → Access granted
```

---

## 🧪 Testing

```bash
mvn test
mvn clean test jacoco:report
```

---

## 🚀 Deployment

### JAR Deployment

```bash
mvn clean package
java -jar target/sutra-ai-backend.jar
```

---

### Docker

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/sutra-ai-backend.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t sutra-ai-backend .
docker run -p 8080:8080 sutra-ai-backend
```

---

## 🗄️ Database Schema (Simplified)

* **Users**
* **Conversations**
* **Messages**

(Relational mapping using foreign keys)

---

## 🤝 Contributing

```bash
git checkout -b feature/your-feature
git commit -m "Added feature"
git push origin feature/your-feature
```

---

## 📄 License

MIT License

---

## 👨‍💻 Author

**Mahesh Swami**

* GitHub: [https://github.com/Mahesh1354](https://github.com/Mahesh1354)
* Project: [https://github.com/Mahesh1354/sutra-ai-backend](https://github.com/Mahesh1354/sutra-ai-backend)

---

<div align="center">
  ⭐ Star this repo if you found it useful!
</div>
