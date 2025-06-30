# System Architecture

## 🏗️ Overview

The Authentication Service follows a layered architecture pattern with Spring Boot, implementing clean architecture principles.

## 📐 Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Controllers Layer                        │
│  AuthController | EmailController | PasswordController      │
│  MfaController  | UserController  | OAuthController         │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Security Layer                           │
│  JWT Filter | CORS Config | Password Encoder | Auth Manager │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Service Layer                            │
│  AuthService | JwtService | MfaService | EmailService       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Repository Layer                         │
│  UserRepository | RefreshTokenRepository | EmailTokenRepo   │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Database Layer                           │
│                    SQLite3 Database                         │
└─────────────────────────────────────────────────────────────┘
```

## 🧩 Core Components

### Controllers
- **AuthController**: Login, signup, logout, token management
- **EmailController**: Email verification workflows
- **PasswordController**: Password reset and change
- **MfaController**: Multi-factor authentication
- **UserController**: User profile operations
- **OAuthController**: OAuth2 provider integration

### Services
- **AuthService**: Core authentication business logic
- **JwtService**: JWT token generation and validation
- **MfaService**: TOTP-based multi-factor authentication
- **EmailService**: Email sending and templates

### Security
- **JwtAuthenticationFilter**: JWT token validation
- **SecurityConfig**: Spring Security configuration
- **PasswordEncoder**: BCrypt password hashing

### Data Access
- **UserRepository**: User data operations
- **RefreshTokenRepository**: Refresh token management
- **EmailVerificationTokenRepository**: Email verification
- **PasswordResetTokenRepository**: Password reset

## 🔄 Key Flows

### Authentication Flow
```
1. Client → POST /auth/login
2. AuthController → AuthService
3. AuthService → AuthenticationManager
4. AuthService → JwtService (generate tokens)
5. Response → Client (JWT tokens)
```

### Protected Request Flow
```
1. Client → Request (with JWT)
2. JwtAuthenticationFilter → JwtService (validate)
3. SecurityContext → Set authentication
4. Controller → Service → Repository
5. Response → Client
```

## 🗄️ Database Schema

### Main Tables
- **users**: Core user data and authentication
- **refresh_tokens**: JWT refresh token storage
- **email_verification_tokens**: Email verification
- **password_reset_tokens**: Password reset
- **user_mfa_backup_codes**: MFA backup codes
- **user_oauth_providers**: OAuth provider links
- **user_roles**: User roles and permissions

### Entity Relationships
```
User (1) ←→ (N) RefreshToken
User (1) ←→ (N) EmailVerificationToken
User (1) ←→ (N) PasswordResetToken
User (1) ←→ (N) MfaBackupCodes
User (1) ←→ (N) OAuthProviders
```

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.2.0**: Application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data access layer
- **Spring Mail**: Email functionality

### Database
- **SQLite3**: Lightweight, file-based database
- **Hibernate**: ORM framework

### Security
- **JWT**: Stateless authentication
- **BCrypt**: Password hashing
- **TOTP**: Multi-factor authentication
- **CORS**: Cross-origin resource sharing

### External Services
- **SMTP**: Email delivery
- **OAuth2**: Third-party authentication

## 🚀 Scalability

### Horizontal Scaling
- **Stateless Design**: JWT enables horizontal scaling
- **Database**: Can migrate to PostgreSQL/MySQL
- **Caching**: Redis for session/token caching
- **Load Balancing**: Multiple instances support

### Performance
- **Connection Pooling**: HikariCP
- **Async Operations**: Email sending
- **Token Expiration**: Configurable lifetimes

## 🔒 Security Features

### Authentication
- **Password Security**: BCrypt with salt
- **Token Security**: JWT with HMAC-SHA256
- **MFA Protection**: TOTP with backup codes
- **Session Management**: Stateless with expiration

### API Security
- **CORS Configuration**: Controlled access
- **Rate Limiting**: Prevent abuse
- **Input Validation**: Comprehensive validation
- **Error Handling**: Secure error messages

## 📊 Monitoring

### Logging
- **Structured Logging**: JSON format
- **Audit Trail**: Authentication events
- **Log Levels**: DEBUG, INFO, WARN, ERROR

### Health Checks
- **Database Connectivity**
- **External Service Status**
- **Application Health Endpoint** 