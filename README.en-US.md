# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)

**Enterprise Microservice Framework (Codex-first maintenance mode)**

English | [简体中文](./README.md)

</div>

## Overview

Goya is an enterprise backend framework built on **Java 25** and **Spring Boot 4.0.1**.
This repository focuses on backend modules. Frontend code is maintained in a separate repository.

## Current State

- Main runnable app: `platform/platform-monolith`
- Default port: `8101`
- Development mode: **Codex-first**
- Quality policy: **No test files policy** (no `src/test/**` additions)

## Quick Start

### Requirements

- JDK 25+
- Maven 3.9+
- Docker (optional)

### Build (skip tests)

```bash
mvn -DskipTests compile
```

### Run monolith

```bash
cd platform/platform-monolith
mvn spring-boot:run
```

Visit: `http://localhost:8101`

### Optional infra bootstrap

```bash
cd deploy/docker/docker-compose/basic
docker-compose up -d
```

## Repository Layout

```text
Goya/
├── AGENTS.md                 # Project-level Codex instructions
├── .agents/skills/           # Project-specific Codex skills
├── docs/                     # Documentation system (entry: docs/SUMMARY.md)
├── bom/
├── component/
├── ai/
├── platform/
├── cloud/
└── deploy/
```

## Documentation Entry

Start here:

- [docs/SUMMARY.md](./docs/SUMMARY.md)

Key docs:

- [Architecture Overview](./docs/architecture/overview.md)
- [Module Map](./docs/architecture/module-map.md)
- [Codex Workflow](./docs/development/codex-workflow.md)
- [No-Test Policy](./docs/development/no-test-policy.md)
- [Build and Release](./docs/operations/build-and-release.md)
- [Changelog](./docs/progress/changelog.md)

## Codex Constraints

- Read `docs/SUMMARY.md` before implementation.
- Validate changes with `-DskipTests` build commands.
- Do not add test files or test dependencies.
- Keep docs synchronized with every functional change.

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

[Apache License 2.0](./LICENSE)
