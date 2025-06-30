# Authentication Service Makefile

.PHONY: help setup run clean test token users

# Default target
help:
	@echo "Authentication Service - Available Commands:"
	@echo ""
	@echo "Setup:"
	@echo "  setup          - Install dependencies and setup project"
	@echo "  dev-up         - Start the application"
	@echo ""
	@echo "Run:"
	@echo "  run            - Run the application"
	@echo "  test           - Run tests"
	@echo "  token          - Generate JWT token for testing"
	@echo "  users          - List users (requires TOKEN)"
	@echo ""
	@echo "Clean:"
	@echo "  clean          - Clean build artifacts"
	@echo "  dev-down       - Stop the application"
	@echo "  test-down      - Stop test environment"

# Setup
setup:
	@echo "Setting up Authentication Service..."
	@if ! command -v java &> /dev/null; then \
		echo "Java not found. Please install Java 17+"; \
		exit 1; \
	fi
	@if ! command -v mvn &> /dev/null; then \
		echo "Maven not found. Please install Maven"; \
		exit 1; \
	fi
	@echo "Installing dependencies..."
	mvn clean install -DskipTests
	@echo "Setup complete!"

# Run application
run:
	@echo "Starting Authentication Service..."
	mvn spring-boot:run

# Start development environment
dev-up:
	@echo "Starting development environment..."
	mvn spring-boot:run

# Stop development environment
dev-down:
	@echo "Stopping development environment..."
	@pkill -f "spring-boot:run" || true
	@echo "Development environment stopped"

# Run tests
test:
	@echo "Running tests..."
	mvn test

# Stop test environment
test-down:
	@echo "Stopping test environment..."
	@pkill -f "mvn test" || true

# Generate JWT token for testing
token:
	@echo "Generating JWT token for testing..."
	@curl -s -X POST http://localhost:8080/auth/signup \
		-H "Content-Type: application/json" \
		-d '{"username":"testuser","email":"test@example.com","password":"TestPass123!","confirmPassword":"TestPass123!","firstName":"Test","lastName":"User"}' \
		| jq -r '.accessToken' 2>/dev/null || echo "Failed to generate token. Make sure the application is running."

# List users (requires TOKEN environment variable)
users:
	@if [ -z "$(TOKEN)" ]; then \
		echo "Error: TOKEN environment variable not set"; \
		echo "Run 'make token' first and export the token:"; \
		echo "export TOKEN=<token_from_make_token>"; \
		exit 1; \
	fi
	@echo "Listing users..."
	@curl -s -X GET http://localhost:8080/user/me \
		-H "Authorization: Bearer $(TOKEN)" \
		| jq '.' 2>/dev/null || echo "Failed to list users. Check if TOKEN is valid and application is running."

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	@rm -f authdb.sqlite
	@rm -rf target/
	@echo "Clean complete!"

# Quick health check
health:
	@echo "Checking application health..."
	@curl -s http://localhost:8080/actuator/health | jq '.' 2>/dev/null || echo "Application not responding"

# Database backup
backup:
	@echo "Creating database backup..."
	@cp authdb.sqlite authdb.sqlite.backup.$(shell date +%Y%m%d_%H%M%S)
	@echo "Backup created: authdb.sqlite.backup.$(shell date +%Y%m%d_%H%M%S)"

# Database restore (usage: make restore BACKUP=filename)
restore:
	@if [ -z "$(BACKUP)" ]; then \
		echo "Error: BACKUP parameter required"; \
		echo "Usage: make restore BACKUP=authdb.sqlite.backup.20240101_120000"; \
		exit 1; \
	fi
	@echo "Restoring database from $(BACKUP)..."
	@cp $(BACKUP) authdb.sqlite
	@echo "Database restored from $(BACKUP)" 