# Authentication Service
A comprehensive Spring Boot authentication service with JWT tokens, MFA support, email verification, and OAuth2 integration.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Installation
1. **Clone and setup**
   ```bash
   git clone https://github.com/sakhileln/authentication.git
   cd authentication
   ```

2. **Configure environment**
   ```bash
   export JWT_SECRET="your-super-secret-jwt-key-that-should-be-at-least-256-bits-long"
   export MAIL_USERNAME="your-email@gmail.com"
   export MAIL_PASSWORD="your-app-password"
   ```

3. **Run the application**
   ```bash
   # Generate a test token
   make token
   
   # Export the token and test user endpoints
   export TOKEN=<token_from_above>
   make users

   # Setup and run
   make setup
   make dev-up
   
   # Clean up
   make dev-down
   make clean
   ```

4. **Access the API**
   - Base URL: `http://localhost:8080`
   - Database: `authdb.sqlite` (created automatically)

## 📚 Documentation
- **[API Reference](docs/api/endpoints.md)** - Complete API documentation
- **[Architecture](docs/architecture/system-design.md)** - System design and components
- **[Security](docs/security/features.md)** - Security features and best practices
- **[Database](docs/database/schema.md)** - Database schema and management
- **[Deployment](docs/deployment/deploy.md)** - Production deployment guide

## 🔧 Features
- 🔐 JWT Authentication with refresh tokens
- 📧 Email verification system
- 🔑 Password reset and management
- 🛡️ Multi-Factor Authentication (TOTP)
- 🔗 OAuth2 integration (Google, GitHub)
- 📊 SQLite3 database
- 🚫 Token revocation and management

## 🛠️ Tech Stack
- **Java 17** + **Spring Boot 3.2.0**
- **Spring Security** + **JWT**
- **Spring Data JPA** + **SQLite3**
- **TOTP** for MFA
- **Spring Mail** for email notifications

## 📖 Usage Examples
### Register a new user
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "sakhile",
    "email": "sakhile@mars.com",
    "password": "SecurePass123!",
    "confirmPassword": "SecurePass123!",
    "firstName": "Sakhile",
    "lastName": "Ndlazi"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sakhile@mars.com",
    "password": "SecurePass123!"
  }'
```

See [API Reference](docs/api/endpoints.md) for complete examples.

## 🔒 Security
- BCrypt password hashing
- JWT with HMAC-SHA256 signing
- Configurable token expiration
- MFA with TOTP and backup codes
- Email verification required
- CORS configuration

## 📝 License
This project is licensed under [license](LISENSE).

## 🤝 Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📞 Support
- 📖 Check the [documentation](docs/)
- 🐛 Create an issue for bugs
- 💡 Suggest features via issues
