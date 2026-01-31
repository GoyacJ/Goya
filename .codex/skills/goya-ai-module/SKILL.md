---
name: goya-ai-module
description: Goya AI module development (Spring AI, LangChain4j, RAG, MCP) with model-agnostic patterns and prompt conventions.
metadata:
  short-description: Goya AI module development
---

# Goya AI Module

Use this skill when working under `ai/` (Spring AI integration, RAG, MCP, model management, or AI services).

## Workflow (concise)
1) Identify the AI submodule (`ai-spring`, `ai-model`, `ai-rag`, `ai-mcp`, `ai-video`) and reuse existing services.
2) Keep business logic model-agnostic; isolate provider-specific clients behind adapters.
3) Ensure AI failures do not break core flows; add fallbacks and clear logging.
4) Control token usage and avoid unbounded prompts or large context windows.

## Key conventions (reminders)
- Use the prompt/RAG patterns defined in the AI rules file.
- Maintain consistent logging (`[Goya] |- ...`) and Chinese JavaDoc comments.
- Prefer structured inputs/outputs for prompts and responses.

## Where to look (load only as needed)
- `.cursor/rules/ai-module.mdc` (prompt and RAG patterns)
- `.cursor/rules/java-backend.mdc` (general Java rules)
- `docs/architecture/modules.md` (module map)
- `ai/ai-rag` for RAG services and vector store patterns
- `ai/ai-mcp` for MCP protocol integration

## Common commands
- Build: `mvn -q -DskipTests clean install`
- Run monolith: `cd platform/platform-monolith && mvn spring-boot:run`
