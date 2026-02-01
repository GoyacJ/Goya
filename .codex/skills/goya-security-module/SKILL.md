---
name: goya-security-module
description: Goya security module development (OAuth2/OIDC, JWT, RBAC) with strict security and audit conventions.
metadata:
  short-description: Goya security development
---

# Goya Security Module

Use this skill when editing authentication/authorization code, OAuth2/OIDC flows, JWT handling, or RBAC-related services.

## Workflow (concise)
1) Identify the target security submodule under `component/component-security` or `platform` security packages.
2) Apply least-privilege defaults and validate all inputs (redirect URIs, token claims, tenant scope).
3) Use strong crypto defaults; avoid weak encoders or plain-text secrets.
4) Record security-relevant events with consistent logging.

## Key conventions (reminders)
- Follow required annotations/logging from `.cursor/rules/java-backend.mdc`.
- Follow security rules for password hashing, token validation, and audit logging.
- Keep token contents minimal; never embed secrets.

## Where to look (load only as needed)
- `.cursor/rules/security-module.mdc`
- `.cursor/rules/java-backend.mdc`
- `component/component-security` (core/authentication/authorization/oauth2)
- `docs/architecture/modules.md` (module map)

## Common commands
- Build: `mvn -q -DskipTests clean install`
- Run monolith: `cd platform/platform-monolith && mvn spring-boot:run`
