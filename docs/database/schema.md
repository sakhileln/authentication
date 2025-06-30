# Database Schema
## 🗄️ Overview
The Authentication Service uses SQLite3 as the primary database, providing a lightweight, file-based solution that's perfect for development and small to medium-scale deployments.

## 📊 Database Configuration
### SQLite3 Setup
```yaml
spring:
  datasource:
    url: jdbc:sqlite:authdb.sqlite
    driver-class-name: org.sqlite.JDBC
    username: 
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.community.dialect.SQLiteDialect
        format_sql: true
```

### Database File
- **Location**: `authdb.sqlite` (project root)
- **Size**: Typically 1-10 MB
- **Backup**: Simple file copy
- **Portability**: Single file deployment

## 🏗️ Database Schema
### Core Tables
#### 1. users
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    profile_image_url VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    email_verified_at TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255)
);
```

#### 2. refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    revoked_by VARCHAR(100),
    user_agent VARCHAR(500),
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 3. email_verification_tokens
```sql
CREATE TABLE email_verification_tokens (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4. password_reset_tokens
```sql
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTOINCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Collection Tables
#### 5. user_mfa_backup_codes
```sql
CREATE TABLE user_mfa_backup_codes (
    user_id BIGINT NOT NULL,
    backup_code VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, backup_code),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 6. user_oauth_providers
```sql
CREATE TABLE user_oauth_providers (
    user_id BIGINT NOT NULL,
    provider VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, provider),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 7. user_roles
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 🔍 Indexes
### Performance Indexes
```sql
-- User lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Token lookups
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_email_verification_tokens_token ON email_verification_tokens(token);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Expiration cleanup
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_email_verification_tokens_expires_at ON email_verification_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
```

## 🔄 Entity Relationships
### One-to-Many Relationships
```
User (1) ←→ (N) RefreshToken
User (1) ←→ (N) EmailVerificationToken
User (1) ←→ (N) PasswordResetToken
```

### Element Collections
```
User (1) ←→ (N) MfaBackupCodes
User (1) ←→ (N) OAuthProviders
User (1) ←→ (N) Roles
```

## 🛠️ Database Management
### SQLite3 Commands
#### Connect to Database
```bash
sqlite3 authdb.sqlite
```

#### View Tables
```sql
.tables
```
#### View Schema
```sql
.schema users
.schema refresh_tokens
.schema email_verification_tokens
.schema password_reset_tokens
```

#### Query Examples
```sql
-- View all users
SELECT id, username, email, email_verified, created_at FROM users;

-- View active refresh tokens
SELECT * FROM refresh_tokens WHERE revoked_at IS NULL;

-- View expired tokens
SELECT * FROM refresh_tokens WHERE expires_at < datetime('now');

-- Count users by verification status
SELECT email_verified, COUNT(*) FROM users GROUP BY email_verified;
```

### Data Cleanup
#### Remove Expired Tokens
```sql
-- Remove expired refresh tokens
DELETE FROM refresh_tokens WHERE expires_at < datetime('now');

-- Remove expired email verification tokens
DELETE FROM email_verification_tokens WHERE expires_at < datetime('now');

-- Remove expired password reset tokens
DELETE FROM password_reset_tokens WHERE expires_at < datetime('now');
```

#### Remove Revoked Tokens
```sql
-- Remove revoked refresh tokens
DELETE FROM refresh_tokens WHERE revoked_at IS NOT NULL;
```

## 📊 Data Types
### SQLite3 Data Types
- **INTEGER**: User IDs, timestamps
- **TEXT**: Usernames, emails, tokens, URLs
- **BOOLEAN**: Flags (stored as INTEGER 0/1)
- **BLOB**: Binary data (if needed)

### JPA Mappings
- **@Id**: Primary key with auto-increment
- **@Column**: Explicit column definitions
- **@Enumerated**: Enum mappings
- **@ElementCollection**: Collection mappings
- **@Temporal**: Date/time mappings

## 🔒 Data Security
### Encryption
- **Passwords**: BCrypt hashed (not encrypted)
- **Tokens**: UUID-based, cryptographically secure
- **Database**: File-level encryption (optional)
- **Backup**: Encrypted backups recommended

### Access Control
- **File Permissions**: Restrict database file access
- **Application Level**: JPA/Hibernate security
- **Connection Pooling**: HikariCP configuration
- **Audit Logging**: Track database operations

## 📈 Performance Optimization
### Query Optimization
- **Indexes**: Strategic indexing on frequently queried columns
- **Batch Operations**: Use batch inserts/updates
- **Connection Pooling**: Configure HikariCP settings
- **Query Caching**: JPA query result caching

### Maintenance
- **VACUUM**: Reclaim unused space
- **ANALYZE**: Update query planner statistics
- **REINDEX**: Rebuild indexes
- **Backup**: Regular database backups

## 🔄 Migration Strategy
### Development to Production
1. **Schema Validation**: Verify schema consistency
2. **Data Migration**: Export/import data if needed
3. **Index Creation**: Ensure all indexes are created
4. **Performance Testing**: Validate query performance

### Version Control
- **Schema Changes**: Track in version control
- **Migration Scripts**: Automated schema updates
- **Rollback Plan**: Ability to revert changes
- **Testing**: Validate migrations in test environment

## 📋 Database Utilities
### Backup Script
```bash
#!/bin/bash
# Backup database
cp authdb.sqlite authdb.sqlite.backup.$(date +%Y%m%d_%H%M%S)
```

### Restore Script
```bash
#!/bin/bash
# Restore database
cp authdb.sqlite.backup.20240101_120000 authdb.sqlite
```

### Cleanup Script
```bash
#!/bin/bash
# Clean expired tokens
sqlite3 authdb.sqlite << EOF
DELETE FROM refresh_tokens WHERE expires_at < datetime('now');
DELETE FROM email_verification_tokens WHERE expires_at < datetime('now');
DELETE FROM password_reset_tokens WHERE expires_at < datetime('now');
VACUUM;
EOF
```

## 🚨 Troubleshooting
### Common Issues
1. **File Permissions**: Ensure read/write access
2. **Disk Space**: Monitor available space
3. **Concurrent Access**: SQLite3 limitations
4. **Performance**: Index optimization needed

### Monitoring
- **File Size**: Monitor database growth
- **Query Performance**: Track slow queries
- **Connection Pool**: Monitor connection usage
- **Error Logs**: Review database errors 