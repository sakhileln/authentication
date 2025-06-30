# Security Features

## 🔐 Authentication Security

### JWT (JSON Web Tokens)
- **Algorithm**: HMAC-SHA256 for signing
- **Token Structure**: Header.Payload.Signature
- **Access Token**: 15 minutes expiration
- **Refresh Token**: 7 days expiration with rotation
- **Claims**: User ID, roles, expiration, issuer

### Password Security
- **Hashing**: BCrypt with salt rounds (10)
- **Requirements**: 
  - Minimum 8 characters
  - Uppercase and lowercase letters
  - Numbers and special characters
- **Validation**: Real-time password strength checking

### Multi-Factor Authentication (MFA)
- **TOTP**: Time-based One-Time Password (RFC 6238)
- **Algorithm**: SHA1 with 6-digit codes
- **Period**: 30-second intervals
- **Backup Codes**: 10 single-use recovery codes
- **QR Code**: Standard otpauth:// format

## 🛡️ API Security

### CORS Configuration
- **Origins**: Configurable allowed origins
- **Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Headers**: Authorization, Content-Type
- **Credentials**: Supported for authenticated requests

### Rate Limiting
- **Login Attempts**: 5 attempts per 15 minutes
- **Password Reset**: 3 requests per hour
- **Email Verification**: 5 requests per hour
- **Token Refresh**: 10 requests per minute

### Input Validation
- **Email**: RFC 5322 compliant validation
- **Username**: 3-50 characters, alphanumeric
- **Password**: Strength requirements enforced
- **Tokens**: UUID format validation

## 🔒 Data Protection

### Token Management
- **Secure Storage**: Database with encryption
- **Revocation**: Immediate token invalidation
- **Rotation**: Automatic refresh token rotation
- **Cleanup**: Expired token removal

### Email Security
- **Verification**: Required for account activation
- **Token Expiration**: 1-hour validity
- **Secure Links**: HTTPS verification URLs
- **Rate Limiting**: Prevent email abuse

### Session Security
- **Stateless**: No server-side sessions
- **Token-Based**: JWT for authentication
- **Automatic Expiration**: Configurable timeouts
- **Secure Logout**: Token revocation

## 🚫 Attack Prevention

### Brute Force Protection
- **Account Lockout**: Temporary after failed attempts
- **Progressive Delays**: Increasing wait times
- **IP Tracking**: Monitor suspicious activity
- **Rate Limiting**: Prevent rapid requests

### Token Security
- **Secure Generation**: Cryptographically random
- **Short Lifespan**: Limited token validity
- **HTTPS Only**: Secure transmission
- **HttpOnly Cookies**: XSS protection

### SQL Injection Prevention
- **JPA/Hibernate**: Parameterized queries
- **Input Sanitization**: Validation and escaping
- **Prepared Statements**: Automatic protection
- **ORM Mapping**: Type-safe queries

## 📧 Email Security

### SMTP Configuration
- **TLS/SSL**: Encrypted email transmission
- **Authentication**: Secure SMTP credentials
- **Rate Limiting**: Prevent email abuse
- **Template Security**: No sensitive data in templates

### Verification Process
- **Unique Tokens**: UUID-based verification
- **Time-Limited**: 1-hour expiration
- **One-Time Use**: Tokens invalidated after use
- **Secure Links**: HTTPS verification URLs

## 🔐 MFA Security

### TOTP Implementation
- **Secret Generation**: Cryptographically secure
- **QR Code**: Standard format for authenticator apps
- **Time Synchronization**: 30-second tolerance
- **Backup Codes**: Secure generation and storage

### Recovery Process
- **Backup Codes**: 10 single-use codes
- **Secure Storage**: Encrypted in database
- **Usage Tracking**: Monitor code usage
- **Regeneration**: Secure code replacement

## 🛡️ OAuth2 Security

### Provider Integration
- **State Parameter**: CSRF protection
- **Secure Callbacks**: HTTPS redirect URIs
- **Token Validation**: Verify provider tokens
- **Account Linking**: Secure provider association

### Authorization Flow
- **PKCE**: Proof Key for Code Exchange
- **Secure Storage**: Encrypted token storage
- **Scope Validation**: Limited permissions
- **Token Refresh**: Secure renewal process

## 📊 Security Monitoring

### Audit Logging
- **Authentication Events**: Login, logout, failures
- **Security Incidents**: Failed attempts, suspicious activity
- **Token Operations**: Creation, refresh, revocation
- **User Actions**: Profile changes, password updates

### Error Handling
- **Generic Messages**: No sensitive information leakage
- **Logging**: Detailed error logs for debugging
- **Rate Limiting**: Prevent error enumeration
- **Graceful Degradation**: Maintain service availability

## 🔧 Security Configuration

### Environment Variables
```bash
# Required
JWT_SECRET=your-super-secret-jwt-key-that-should-be-at-least-256-bits-long

# Optional
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### Application Properties
```yaml
# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  access-token-validity: 900  # 15 minutes
  refresh-token-validity: 604800  # 7 days

# Security Configuration
app:
  auth:
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-numbers: true
      require-special-chars: true
```

## 🚨 Security Best Practices

### Development
1. **Never commit secrets** to version control
2. **Use environment variables** for sensitive data
3. **Validate all inputs** thoroughly
4. **Implement proper error handling**
5. **Use HTTPS** in production

### Deployment
1. **Secure database** with proper access controls
2. **Enable HTTPS** with valid certificates
3. **Configure firewalls** and network security
4. **Monitor logs** for security events
5. **Regular updates** of dependencies

### Maintenance
1. **Rotate secrets** regularly
2. **Monitor security advisories**
3. **Update dependencies** promptly
4. **Backup data** securely
5. **Test security measures** regularly 