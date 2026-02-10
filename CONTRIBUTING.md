# Contributing to Goya

## Scope

This repository is maintained in **Codex-first** mode.
All contributors should follow `AGENTS.md` and the documentation system under `docs/`.

## Required Reading

Before any change:

1. `docs/SUMMARY.md`
2. `docs/development/codex-workflow.md`
3. `docs/architecture/dependency-rules.md`
4. `docs/development/no-test-policy.md`

## Development Rules

1. Keep changes minimal and module-scoped.
2. Do not commit secrets, passwords, or tokens.
3. Do not add test files (`src/test/**`).
4. Do not add test dependencies (JUnit, Mockito, Testcontainers, etc.).
5. Do not use `mvn test` as a merge gate in this repository.

## Validation

Use build validation with tests skipped:

```bash
# Full repository
mvn -DskipTests compile

# Targeted module
mvn -pl <module> -am -DskipTests validate
```

## Documentation Sync

Every functional/config/build change must update docs:

- At minimum: `docs/progress/changelog.md`
- If module structure changed: `docs/architecture/module-map.md`
- If workflow/commands changed: `docs/development/codex-workflow.md` or `docs/operations/build-and-release.md`

## Commit Message Convention

Use Conventional Commits:

- `feat(scope): ...`
- `fix(scope): ...`
- `refactor(scope): ...`
- `docs(scope): ...`
- `chore(scope): ...`

Examples:

- `fix(platform): normalize monolith startup entry`
- `docs(architecture): refresh module map for codex init`

## Pull Request Checklist

- [ ] Scope is limited to the intended module(s)
- [ ] No plaintext secrets were introduced
- [ ] No test files or test dependencies were added
- [ ] Build validation passed with `-DskipTests`
- [ ] Documentation updated (including changelog)
