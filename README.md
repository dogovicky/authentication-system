# Authentication and Identity System

A production-ready, open-source authentication and identity management system built with Java and Spring Boot.

The system provides secure authentication flows including email verification, password reset, OAuth2 login, session management, and adaptive multi-factor authentication (MFA) using OTP verification when device changes are detected.

---

## Features

### Authentication & Authorization
- User Registration
- Secure Login
- JWT Authentication
- Refresh Token Support
- Role-Based Access Control (RBAC)
- Session Management

### Email & Verification
- Email Verification on Registration
- Resend Verification Email
- Forgot Password Flow
- Password Reset via Email Token

### Security Features
- Adaptive Multi-Factor Authentication (MFA)
- OTP Verification on Device Change
- Secure Password Hashing
- Token Expiration & Revocation
- Brute Force Protection
- Login Attempt Tracking

### OAuth2 Authentication
- Google Login
- GitHub Login
- OAuth2 Client Support

### Session & Device Management
- Active Session Tracking
- Device Fingerprinting
- Session Revocation
- Logout from All Devices
- Suspicious Login Detection

### Event-Driven Architecture
- Kafka Integration
- RabbitMQ Integration
- Asynchronous Email Notifications
- Audit & Security Event Publishing

---

# 🛠Tech Stack

| Technology      | Purpose                        |
|-----------------|--------------------------------|
| Java 23+        | Core Programming Language      |
| Spring Boot     | Backend Framework              |
| Spring Security | Authentication & Authorization |
| PostgreSQL      | Primary Database               |
| Redis           | Caching & Session Storage      |
| RabbitMQ        | Async Messaging                |
| Apache Kafka    | Event Streaming                |
| JWT             | Token-Based Authentication     |
| Docker          | Containerization               |
| Maven           | Dependency Management          |

---

## Project Structure

```bash
src
├── auth
├── config
├── security
├── user
├── session
├── otp
├── oauth
├── notification
├── audit
├── messaging
├── exception
└── util
```

---

# Core Authentication Flows

## 1. User Registration

```text
User → Register → Email Verification Token Generated
     → Verification Email Sent
     → User Verifies Email
     → Account Activated
```

---

## 2. Login Flow

```text
User → Login
     → Credentials Validated
     → Device Checked
     → MFA Triggered (if new device)
     → JWT Tokens Generated
     → Session Stored in Redis
```

---

## 3. Password Reset Flow

```text
User → Forgot Password
     → Reset Token Generated
     → Email Sent
     → User Resets Password
     → Previous Sessions Revoked
```

---

## 4. OAuth2 Login Flow

```text
User → OAuth2 Provider (Google/GitHub)
     → Authentication Successful
     → Internal User Linked/Created
     → JWT Issued
```

---

# Adaptive MFA (Device-Based OTP)

This system supports adaptive MFA.

If a login attempt originates from:
- a new browser,
- unknown device,
- different IP address,
- or suspicious activity,

the user is required to verify an OTP before access is granted.

### MFA Flow

```text
Known Device?
    ├── Yes → Login Success
    └── No
         ├── Generate OTP
         ├── Send OTP via Email/Phone
         └── Verify OTP → Login Success
```

---

# Event-Driven Messaging

The system publishes events using Kafka and RabbitMQ.

## Example Events

### Kafka
- UserRegisteredEvent
- UserLoggedInEvent
- PasswordResetEvent
- MFARequestedEvent

### RabbitMQ
- SendVerificationEmailEvent
- SendOTPEvent
- SendPasswordResetEmailEvent

---

#  Database Design

## Main Entities

- users
- roles
- privileges
- sessions
- refresh_tokens
- verification_tokens
- password_reset_tokens
- otp_codes
- devices
- audit_logs

---

# Security Practices

- BCrypt Password Encoding
- Short-Lived Access Tokens
- Refresh Token Rotation
- Redis Session Blacklisting
- CSRF Protection
- Secure HTTP Headers
- Rate Limiting
- Token Revocation
- Device Fingerprinting

---

# Environment Variables

Create a `.env` file:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/auth_db
DB_USERNAME=postgres
DB_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600000

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Mail
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-password

# OAuth2
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=
```

---

# Running the Application

## Clone Repository

```bash
git clone https://github.com/your-username/authentication-system.git
cd authentication-system
```

## Start Dependencies

```bash
docker-compose up -d
```

## Run Application

```bash
./mvnw spring-boot:run
```

---

# Docker Support

```bash
docker-compose up --build
```

---

# API Endpoints

## Authentication

| Method | Endpoint                | Description   |
|--------|-------------------------|---------------|
| POST   | `/api/v1/auth/register` | Register user |
| POST   | `/api/v1/auth/login`    | Login         |
| POST   | `/api/v1/auth/refresh`  | Refresh token |
| POST   | `/api/v1/auth/logout`   | Logout        |

---

## Verification

| Method | Endpoint                           | Description         |
|--------|------------------------------------|---------------------|
| POST   | `/api/v1/auth/verify-email`        | Verify email        |
| POST   | `/api/v1/auth/resend-verification` | Resend verification |

---

## Password Reset

| Method | Endpoint                       | Description    |
|--------|--------------------------------|----------------|
| POST   | `/api/v1/auth/forgot-password` | Request reset  |
| POST   | `/api/v1/auth/reset-password`  | Reset password |

---

## MFA

| Method | Endpoint                 | Description |
|--------|--------------------------|-------------|
| POST   | `/api/v1/mfa/verify-otp` | Verify OTP  |
| POST   | `/api/v1/mfa/resend-otp` | Resend OTP  |

---

# Testing

Run tests using:

```bash
./mvnw test
```

---

# 📈 Future Improvements

- SMS-Based MFA
- WebAuthn / Passkeys
- Biometric Authentication
- Admin Dashboard
- Account Lockout Policies
- API Key Authentication
- Multi-Tenant Support

---

# Contributing

Contributions are welcome!

1. Fork the project
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

# License

This project is licensed under the MIT License.

---

# Support

If you found this project helpful:
- Star the repository
- Fork the project
- Share with others

---

# Author

Built with ❤️ using Java & Spring Boot.