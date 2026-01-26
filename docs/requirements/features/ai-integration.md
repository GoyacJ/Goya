# AI 集成需求 | AI Integration Requirements

## 1. 功能概述

整合 Spring AI 和 LangChain4j，为 Goya 框架提供开箱即用的 AI 能力，包括对话、RAG、Agent 等功能。

## 2. 核心功能

### 2.1 多模型支持

**支持的模型**：
- OpenAI (GPT-4, GPT-3.5)
- 阿里通义千问
- 本地模型（Ollama）

**统一接口**：
```java
@Service
public class AiService {
    @Autowired
    private ChatClient chatClient;
    
    public String chat(String userMessage) {
        return chatClient.call(userMessage);
    }
}
```

### 2.2 RAG 检索增强生成

**功能**：
- 文档加载（PDF, Word, Markdown）
- 文档分块
- 向量化存储
- 语义检索
- 答案生成

**使用场景**：
- 企业知识库问答
- 文档智能检索
- 客服机器人

### 2.3 Prompt 模板管理

**功能**：
- 模板定义
- 参数替换
- 版本管理

### 2.4 Function Calling

**功能**：
- 工具定义
- 自动调用
- 结果组合

## 3. 技术实现

### 3.1 Spring AI 集成

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4
```

### 3.2 LangChain4j 集成

```java
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName("gpt-4")
    .build();
```

## 4. 用户故事

**作为**开发者  
**我想要**简单地集成 AI 对话能力  
**以便于**快速构建智能应用

**验收标准**：
- 5 行代码完成 AI 对话
- 支持流式响应
- 支持上下文管理

## 5. 参考资料

- [Spring AI 文档](https://docs.spring.io/spring-ai/)
- [LangChain4j 文档](https://docs.langchain4j.dev/)
