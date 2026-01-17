# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gotchan is a Spring Boot 4.0 application written in Kotlin, using JPA for persistence and Redis for caching/messaging.

## Build Commands

```bash
# Build the project
./gradlew build

# Run the application (Docker Compose starts MySQL and Redis automatically)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.gotchan.GotchanApplicationTests"

# Run a single test method
./gradlew test --tests "com.gotchan.GotchanApplicationTests.contextLoads"

# Clean build
./gradlew clean build
```

## Architecture

- **Language**: Kotlin with Java 21 toolchain
- **Framework**: Spring Boot 4.0.1
- **Persistence**: Spring Data JPA with MySQL (production) and H2 (development/testing)
- **Caching**: Spring Data Redis
- **Build**: Gradle with Kotlin DSL plugins

## Development Setup

The project uses Spring Boot Docker Compose support. Running the application automatically starts:
- MySQL on port 3306
- Redis on port 6379

No manual Docker setup required when using `./gradlew bootRun`.

## Kotlin/JPA Configuration

The `allOpen` plugin is configured to open JPA entity classes (`@Entity`, `@MappedSuperclass`, `@Embeddable`) for proper proxy generation.

## Development Methodology: TDD

This project follows Test-Driven Development. See `TDD_GUIDE.md` for detailed guidelines.

**Required development cycle:**
1. RED: Write a failing test first
2. GREEN: Write minimal code to pass the test
3. REFACTOR: Improve code while keeping tests green

**Test execution:**
```bash
./gradlew test                                    # All tests
./gradlew test --tests "*.GachaItemTest"          # Specific class
./gradlew test --tests "*.GachaItemTest.메서드명"  # Specific method
```

## Hexagonal Architecture

```
com.gotchan
├── common/              # Shared utilities (config, exception, response)
├── domain/              # Core business logic
│   └── {domain}/
│       ├── model/       # Entities, Value Objects
│       └── port/        # Repository interfaces (output ports)
├── application/         # Use cases
│   └── {domain}/
│       └── service/     # Application services
└── adapter/
    ├── in/web/          # Controllers (input adapters)
    └── out/persistence/ # JPA implementations (output adapters)
```

## Git Convention

See `GIT_CONVENTION.md` for commit message format and branch naming rules.

**Commit message format:**
```
<type>(<scope>): <subject>
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`
