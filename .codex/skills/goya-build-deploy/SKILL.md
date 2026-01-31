---
name: goya-build-deploy
description: Goya build and deployment workflows for backend (Maven) and frontend (pnpm), plus Docker/Compose.
metadata:
  short-description: Goya build/deploy
---

# Goya Build & Deploy

Use this skill for build, packaging, or deployment changes (Maven builds, frontend bundles, Docker/Compose files).

## Workflow (concise)
1) Pick target runtime: backend JAR, frontend static bundle, or Docker/Compose.
2) Use the documented Maven and pnpm scripts; avoid introducing ad-hoc commands.
3) Keep environment-specific config in `application-*.yml` and `.env.*` files.

## Where to look (load only as needed)
- `docs/guides/quick-start.md` (local run)
- `docs/guides/deployment.md` (build + deploy steps)
- `deploy/docker/docker-compose/basic` (infra compose)
- `platform/platform-monolith` (backend runtime)
- `goya-web-ui/apps/*` (frontend runtime)

## Common commands
- Backend package: `mvn clean package -DskipTests -P prod`
- Backend run: `cd platform/platform-monolith && mvn spring-boot:run`
- Frontend build: `cd goya-web-ui && pnpm build:antd`
- Compose up: `cd deploy/docker/docker-compose/basic && docker-compose up -d`
