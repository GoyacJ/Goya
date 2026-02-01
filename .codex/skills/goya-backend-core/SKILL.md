---
name: goya-backend-core
description: Goya Java/Spring Boot backend development, including component modules, platform services, conventions, and logging/annotation rules.
metadata:
  short-description: Goya backend development
---

# Goya Backend Core

Use this skill when editing or adding Java backend code in Goya, including `component/`, `platform/`, `cloud/`, and shared framework modules.

## Workflow (concise)
1) Locate the target module and scan for similar classes before adding new ones.
2) Follow Goya conventions for naming, annotations, logging, and JavaDoc.
3) Prefer existing abstractions (`Response<T>`, `CacheService`, `I*` interfaces) over bespoke helpers.
4) For auto-configuration, keep a clean bean registration sequence with trace logs.
5) Update the module `pom.xml` when adding dependencies; prefer BOM-managed versions.

## Key conventions (reminders)
- Package: `com.ysmjjsy.goya.{module}.{layer}`
- Required annotations: `@Slf4j`, `@RequiredArgsConstructor`, `@Transactional(rollbackFor = Exception.class)` where applicable
- No field injection (`@Autowired` is disallowed)
- Comments must be in Chinese; public classes/methods need JavaDoc
- Logging style (component/bean):
  - `[Goya] |- component [xxx] XxxAutoConfiguration auto configure.`
  - `[Goya] |- component [xxx] |- bean [yyy] register.`

## Where to look (load only as needed)
- `.cursorrules`
- `.cursor/rules/java-backend.mdc`
- `.cursor/rules/database.mdc` (DB conventions)
- `.cursor/rules/testing.mdc` (test conventions)
- `docs/guides/development.md` (module creation steps)
- `docs/architecture/modules.md` (module map)
- Example auto-config: `component/component-redis/src/main/java/com/ysmjjsy/goya/component/cache/redis/autoconfigure/GoyaRedisAutoConfiguration.java`

## Common commands
- Build: `mvn -q -DskipTests clean install`
- Run monolith: `cd platform/platform-monolith && mvn spring-boot:run`
