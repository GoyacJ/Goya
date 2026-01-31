---
name: goya-frontend-ui
description: Goya web UI development (Vue 3 + TypeScript + pnpm monorepo) across Ant Design Vue, Element Plus, and Naive UI apps.
metadata:
  short-description: Goya frontend development
---

# Goya Frontend UI

Use this skill for changes under `goya-web-ui/`, including apps and shared packages.

## Workflow (concise)
1) Pick the target UI app: `apps/web-antd`, `apps/web-ele`, or `apps/web-naive`.
2) Put reusable logic in `packages/` (shared stores, utils, types, ui-kit) when it should be shared across apps.
3) Follow Vue 3 + TypeScript conventions (script setup, Pinia setup stores, API naming).
4) Keep API calls and stores consistent with backend response types.

## Key conventions (reminders)
- Use `<script setup lang="ts">`.
- Store naming: `use*Store` with setup-style `defineStore`.
- API function naming: `xxxApi()` (e.g., `loginApi`).
- Prefer shared packages over per-app duplication.

## Where to look (load only as needed)
- `.cursorrules`
- `goya-web-ui/apps/*/src/views/_core/README.md` (view structure)
- `goya-web-ui/packages/@core` and `goya-web-ui/packages/*` (shared code)
- `docs/guides/quick-start.md` (frontend run steps)

## Common commands
- Install: `cd goya-web-ui && pnpm install`
- Dev: `pnpm dev:antd` | `pnpm dev:ele` | `pnpm dev:naive`
- Build: `pnpm build:antd` | `pnpm build:ele` | `pnpm build:naive`
