# API Endpoints

Complete API documentation for the Authentication Service.

## 🔐 Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## 📋 Endpoints Overview

| Category | Endpoints | Description |
|----------|-----------|-------------|
| [Auth Core](#auth-core) | 7 endpoints | Login, signup, logout, token management |
| [Email Verification](#email-verification) | 3 endpoints | Email verification system |
| [Password Management](#password-management) | 3 endpoints | Password reset and change |
| [MFA / 2FA](#mfa-2fa) | 6 endpoints | Multi-factor authentication |
| [User Profile](#user-profile) | 3 endpoints | User profile management |
| [OAuth2](#oauth2) | 5 endpoints | OAuth provider integration |

---

## 🔐 Auth Core

### POST /auth/login
Log in with email and password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "mfaCode": "123456"  // Optional, required if MFA is enabled
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "uuid-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "username": "sakhile",
    "email": "user@example.com",
    "firstName": "Sakhile",
    "lastName": "Ndlazi",
    "emailVerified": true,
    "mfaEnabled": false
  },
  "mfaRequired": false
}
```

### POST /auth/signup
Register a new user account.

**Request:**
```json
{
  "username": "sakhile",
  "email": "user@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "firstName": "Sakhile",
  "lastName": "Ndlazi"
}
```

**Response:** Same as login response

### POST /auth/logout
Logout and invalidate refresh token.

**Request:**
```
refreshToken=uuid-refresh-token
```

**Response:**
```json
{
  "message": "Successfully logged out"
}
```

### POST /auth/refresh
Refresh JWT access token using refresh token.

**Request:**
```
refreshToken=uuid-refresh-token
```

**Response:** Same as login response

### GET /auth/me
Get current authenticated user information.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "id": 1,
  "username": "sakhile",
  "email": "user@example.com",
  "firstName": "Sakhile",
  "lastName": "Ndlazi",
  "profileImageUrl": "https://example.com/avatar.jpg",
  "emailVerified": true,
  "emailVerifiedAt": "2024-01-01T10:00:00",
  "lastLoginAt": "2024-01-01T10:00:00",
  "createdAt": "2024-01-01T10:00:00",
  "mfaEnabled": false
}
```

### GET /auth/session
Get current session metadata.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "authenticated": true,
  "user": "user@example.com",
  "authorities": ["ROLE_USER"],
  "remoteAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0..."
}
```

### POST /auth/revoke
Revoke all refresh tokens for the current user.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "message": "All tokens revoked successfully"
}
```

---

## 📧 Email Verification

### POST /auth/email/verify/request
Request email verification link.

**Request:**
```
email=user@example.com
```

**Response:**
```json
{
  "message": "Email verification link sent successfully"
}
```

### POST /auth/email/verify/confirm
Confirm email verification with token.

**Request:**
```
token=email-verification-token
```

**Response:**
```json
{
  "message": "Email verified successfully"
}
```

### GET /auth/email/verify/status
Check email verification status.

**Request:**
```
email=user@example.com
```

**Response:**
```json
{
  "verified": true
}
```

---

## 🔑 Password Management

### POST /auth/password/forgot
Request password reset email.

**Request:**
```
email=user@example.com
```

**Response:**
```json
{
  "message": "Password reset email sent successfully"
}
```

### POST /auth/password/reset
Reset password using token from email.

**Request:**
```
token=password-reset-token&newPassword=NewSecurePass123!
```

**Response:**
```json
{
  "message": "Password reset successfully"
}
```

### POST /auth/password/change
Change password for authenticated user.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
currentPassword=OldPass123!&newPassword=NewSecurePass123!
```

**Response:**
```json
{
  "message": "Password changed successfully"
}
```

---

## 🛡️ MFA / 2FA

### POST /auth/mfa/setup
Generate MFA QR code and secret for setup.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCodeUrl": "otpauth://totp/Authentication%20Service:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Authentication%20Service&algorithm=SHA1&digits=6&period=30",
  "message": "MFA setup initiated. Scan the QR code with your authenticator app."
}
```

### POST /auth/mfa/verify
Verify MFA setup with TOTP code.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
code=123456&secret=JBSWY3DPEHPK3PXP
```

**Response:**
```json
{
  "message": "MFA setup completed successfully",
  "backupCodes": "ABC12345, DEF67890, GHI11111, JKL22222, MNO33333"
}
```

### POST /auth/mfa/enable
Enable MFA for user account.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "message": "MFA enabled successfully"
}
```

### POST /auth/mfa/disable
Disable MFA for user account.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
code=123456
```

**Response:**
```json
{
  "message": "MFA disabled successfully"
}
```

### POST /auth/mfa/challenge
Challenge user for MFA during login.

**Request:**
```
email=user@example.com&code=123456
```

**Response:**
```json
{
  "message": "MFA challenge completed"
}
```

### POST /auth/mfa/recovery
Use backup code for MFA recovery.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
backupCode=ABC12345
```

**Response:**
```json
{
  "message": "Backup code used successfully"
}
```

---

## 👤 User Profile

### GET /user/me
Get current user profile information.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "id": 1,
  "username": "sakhile",
  "email": "user@example.com",
  "firstName": "Sakhile",
  "lastName": "Ndlazi",
  "profileImageUrl": "https://example.com/avatar.jpg",
  "emailVerified": true,
  "emailVerifiedAt": "2024-01-01T10:00:00",
  "lastLoginAt": "2024-01-01T10:00:00",
  "createdAt": "2024-01-01T10:00:00",
  "mfaEnabled": false,
  "oauthProviders": ["google"]
}
```

### PUT /user/profile
Update user profile information.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
firstName=Sakhile&lastName=Ndlazi&profileImageUrl=https://example.com/avatar.jpg
```

**Response:**
```json
{
  "message": "Profile updated successfully"
}
```

### PUT /user/email
Change user email address.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
newEmail=newemail@example.com&password=CurrentPass123!
```

**Response:**
```json
{
  "message": "Email changed successfully. Please verify your new email."
}
```

---

## 🔗 OAuth2

### GET /auth/oauth/{provider}
Redirect to OAuth provider (Google, GitHub, etc.).

**Response:**
```json
{
  "message": "Redirecting to google OAuth provider",
  "provider": "google",
  "redirectUrl": "/oauth2/authorization/google"
}
```

### GET /auth/oauth/{provider}/callback
Handle OAuth callback from provider.

**Request:**
```
code=oauth-authorization-code&state=state-parameter
```

**Response:**
```json
{
  "message": "OAuth authentication successful",
  "provider": "google",
  "code": "oauth-authorization-code"
}
```

### POST /auth/oauth/token
Exchange authorization code for access token.

**Request:**
```
provider=google&code=oauth-authorization-code
```

**Response:**
```json
{
  "message": "Token exchange successful",
  "provider": "google",
  "accessToken": "oauth_access_token",
  "refreshToken": "oauth_refresh_token"
}
```

### POST /auth/oauth/link
Link OAuth provider to existing user account.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
provider=google&accessToken=oauth_access_token
```

**Response:**
```json
{
  "message": "OAuth provider linked successfully",
  "provider": "google",
  "userId": "1"
}
```

### POST /auth/oauth/unlink
Unlink OAuth provider from user account.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```
provider=google
```

**Response:**
```json
{
  "message": "OAuth provider unlinked successfully",
  "provider": "google",
  "userId": "1"
}
```

---

## 📊 Response Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

## 🔧 Error Responses

All error responses follow this format:

```json
{
  "error": "Error message description",
  "timestamp": "2024-01-01T10:00:00",
  "path": "/auth/login"
}
```

## 📝 Usage Examples

### Complete Authentication Flow

```bash
# 1. Register user
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

# 2. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sakhile@mars.com",
    "password": "SecurePass123!"
  }'

# 3. Use token for authenticated requests
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Refresh token when expired
curl -X POST http://localhost:8080/auth/refresh \
  -d "refreshToken=YOUR_REFRESH_TOKEN"

# 5. Logout
curl -X POST http://localhost:8080/auth/logout \
  -d "refreshToken=YOUR_REFRESH_TOKEN"
```

### MFA Setup Flow

```bash
# 1. Setup MFA
curl -X POST http://localhost:8080/auth/mfa/setup \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. Verify MFA setup
curl -X POST http://localhost:8080/auth/mfa/verify \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d "code=123456&secret=MFA_SECRET"

# 3. Login with MFA
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sakhile@mars.com",
    "password": "SecurePass123!",
    "mfaCode": "123456"
  }'
``` 