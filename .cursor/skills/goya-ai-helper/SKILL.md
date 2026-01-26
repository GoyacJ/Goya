# Goya AI 集成辅助

## 描述
辅助集成和使用 Spring AI、LangChain4j 等 AI 能力。

## 使用场景
- 配置 AI 模型
- 实现 RAG 功能
- 创建 Prompt 模板
- Function Calling
- AI 对话集成

## 功能清单

### 1. 配置 AI 模型

示例："配置 OpenAI GPT-4"

生成配置和服务类

### 2. RAG 实现

示例："实现文档问答 RAG"

生成：
- DocumentLoaderService
- VectorStoreService
- RagService
- 完整 RAG 流程

### 3. Prompt 模板

示例："创建客服对话 Prompt"

生成 Prompt 模板和管理类

### 4. Function Calling

示例："创建查询天气的 Function"

生成 Function 定义和调用逻辑

## 最佳实践

- 缓存 AI 响应
- 控制 Token 使用
- 添加降级方案
- 流式输出提升体验
- 监控成本和性能

## 参考资料

- Spring AI 文档
- LangChain4j 文档
- OpenAI 最佳实践
