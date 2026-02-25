# 架构概览

## 分层结构
- `component/`：基础框架能力与通用组件。
- `ai/`：AI 能力聚合与扩展模块。
- `platform/`：业务承载层（当前主入口为 `platform-monolith`）。
- `cloud/`：云原生占位模块。

## 当前状态（2026-02-10）
- `src/main/java` 文件总数约 897。
- 运行入口：`platform/platform-monolith`。
- 单体默认端口：`8101`。
- `platform-distributed` 与 `cloud` 仍为骨架状态。

## 设计原则
1. 聚合模块只做模块编排，不承载可依赖实现。
2. 应用层优先依赖 `framework-boot-starter` 统一入口。
3. 自动配置必须显式注册，避免“有类无生效”。
