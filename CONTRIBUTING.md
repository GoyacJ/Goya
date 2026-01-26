# 贡献指南 | Contributing Guide

感谢您对 Goya 项目的关注！我们欢迎所有形式的贡献，包括但不限于代码、文档、问题反馈、功能建议等。

Thank you for your interest in the Goya project! We welcome all forms of contributions, including but not limited to code, documentation, issue reports, and feature suggestions.

[中文](#中文) | [English](#english)

---

## 中文

### 行为准则

参与本项目即表示您同意遵守我们的行为准则：
- 尊重所有贡献者
- 保持专业和友好的交流
- 接受建设性的批评
- 专注于对项目最有利的事情

### 如何贡献

#### 1. 报告问题

在提交问题之前，请确保：
- 搜索现有的 Issue，避免重复
- 使用清晰的标题描述问题
- 提供详细的复现步骤
- 包含必要的环境信息（操作系统、JDK 版本、Spring Boot 版本等）
- 附上相关的日志或错误信息

**Issue 模板**：
```markdown
### 问题描述
简要描述遇到的问题

### 复现步骤
1. 执行...
2. 访问...
3. 看到错误...

### 预期行为
描述您期望发生什么

### 实际行为
描述实际发生了什么

### 环境信息
- OS: [e.g. macOS 13.0]
- JDK: [e.g. OpenJDK 25]
- Spring Boot: [e.g. 4.0.1]
- Goya Version: [e.g. 1.0.0]

### 其他信息
任何其他有助于理解问题的信息
```

#### 2. 提交功能建议

我们欢迎新功能建议！请：
- 清楚描述功能的用例
- 说明为什么这个功能对项目有价值
- 如果可能，提供设计方案或原型

#### 3. 提交代码

##### 前置准备

1. **Fork 项目**
   ```bash
   # 在 GitHub 上 Fork 项目到您的账号
   ```

2. **克隆代码**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Goya.git
   cd Goya
   ```

3. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/your-bug-fix
   ```

##### 开发规范

###### Java 代码规范

1. **命名约定**
   - 包名：`com.ysmjjsy.goya.{module}.{layer}`
   - 类名：大驼峰 `PascalCase`
   - 方法名和变量名：小驼峰 `camelCase`
   - 常量：全大写 `UPPER_SNAKE_CASE`
   - 常量接口：`I*Constants`
   - 枚举：`*Enum`
   - 异常：`*Exception`
   - 配置类：`*AutoConfiguration`

2. **必须使用的注解**
   ```java
   @Slf4j                          // 日志（必须）
   @RequiredArgsConstructor        // 构造器注入
   @Transactional(rollbackFor = Exception.class)  // 事务
   ```

3. **日志格式**
   ```java
   log.debug("[Goya] |- component [xxx] XxxAutoConfiguration auto configure.");
   log.trace("[Goya] |- component [xxx] |- bean [xxxService] register.");
   ```

4. **注释规范**
   - 所有注释使用**中文**
   - 类注释：功能描述 + 使用示例 + `@author goya` + `@since 日期` + `@see`
   - 方法注释：功能描述 + `@param` + `@return` + `@throws`
   - 参考官方文档时注明链接

   ```java
   /**
    * 用户认证转换器
    * <p>
    * 将 HTTP 请求转换为认证令牌
    *
    * @author goya
    * @since 2025-01-24
    * @see org.springframework.security.web.authentication.AuthenticationConverter
    */
   @Slf4j
   @RequiredArgsConstructor
   public class LoginAuthenticationConverter implements AuthenticationConverter {
       
       /**
        * 转换认证请求
        *
        * @param request HTTP 请求
        * @return 认证令牌，如果无法转换则返回 null
        */
       @Override
       public Authentication convert(HttpServletRequest request) {
           // 实现代码
       }
   }
   ```

5. **禁止事项**
   - 禁止在属性名中使用下划线
   - 禁止使用尾行注释
   - 禁止提交包含秘钥、密码等敏感信息的代码

###### TypeScript/Vue 代码规范

1. **命名约定**
   - 组件文件：大驼峰 `PascalCase.vue`
   - 工具函数文件：小驼峰 `camelCase.ts`
   - API 函数：`xxxApi()` (如 `loginApi`, `getUserInfoApi`)
   - Store：`use*Store` (如 `useAuthStore`)

2. **Vue 规范**
   - 优先使用 `<script setup>` 语法
   - Store 使用 Setup 风格：
     ```typescript
     export const useAuthStore = defineStore('auth', () => {
       const token = ref('')
       return { token }
     })
     ```

##### 提交代码

1. **代码质量检查**
   ```bash
   # Java 后端
   cd Goya
   mvn clean verify
   
   # Vue 前端
   cd Goya-Web
   pnpm lint
   pnpm typecheck
   ```

2. **提交变更**
   ```bash
   git add .
   git commit -m "feat: 添加用户登录功能"
   ```

   **提交信息规范**（遵循 Conventional Commits）：
   - `feat:` 新功能
   - `fix:` 修复 Bug
   - `docs:` 文档更新
   - `style:` 代码格式调整
   - `refactor:` 重构
   - `perf:` 性能优化
   - `test:` 测试相关
   - `chore:` 构建/工具链相关

3. **推送到远程**
   ```bash
   git push origin feature/your-feature-name
   ```

4. **创建 Pull Request**
   - 在 GitHub 上创建 PR
   - 填写清晰的 PR 描述
   - 关联相关的 Issue
   - 等待代码审查

##### Pull Request 指南

**PR 标题格式**：
```
类型(范围): 简短描述

例如：
feat(security): 添加短信登录支持
fix(cache): 修复多级缓存同步问题
docs(readme): 更新快速开始指南
```

**PR 描述模板**：
```markdown
## 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 文档更新
- [ ] 重构
- [ ] 性能优化
- [ ] 测试
- [ ] 其他

## 变更说明
详细描述您的变更内容

## 关联 Issue
Closes #123

## 测试
- [ ] 添加了单元测试
- [ ] 添加了集成测试
- [ ] 手动测试通过

## 截图/录屏
（如果适用）

## Checklist
- [ ] 代码遵循项目规范
- [ ] 自测通过
- [ ] 文档已更新
- [ ] 无冲突
```

#### 4. 改进文档

文档同样重要！您可以：
- 修正拼写或语法错误
- 改进现有文档的清晰度
- 添加示例或教程
- 翻译文档

### 开发环境设置

#### 后端环境

```bash
# 1. 克隆代码
git clone https://github.com/GoyaDo/Goya.git
cd Goya/Goya

# 2. 安装依赖
mvn clean install -DskipTests

# 3. 启动基础设施（Docker）
cd doc/docker/docker-compose/basic
docker-compose up -d

# 4. 启动应用
cd ../../../../platform/platform-monolith/auth-server
mvn spring-boot:run
```

#### 前端环境

```bash
# 1. 进入前端目录
cd Goya-Web

# 2. 安装 pnpm（如果未安装）
npm install -g pnpm

# 3. 安装依赖
pnpm install

# 4. 启动开发服务器
pnpm dev:antd
```

### 代码审查流程

1. 提交 PR 后，维护者会进行代码审查
2. 根据反馈进行修改
3. 所有检查通过后，PR 将被合并
4. 您的贡献将出现在 Contributors 列表中

### 发布流程

项目采用语义化版本控制（Semantic Versioning）：
- 主版本号：不兼容的 API 变更
- 次版本号：向后兼容的功能性新增
- 修订号：向后兼容的问题修正

### 社区

- **GitHub Discussions**: 技术讨论和问答
- **GitHub Issues**: Bug 报告和功能请求

### 许可证

通过贡献代码，您同意您的贡献将在 Apache License 2.0 下授权。

---

## English

### Code of Conduct

By participating in this project, you agree to abide by our code of conduct:
- Respect all contributors
- Maintain professional and friendly communication
- Accept constructive criticism
- Focus on what's best for the project

### How to Contribute

#### 1. Reporting Issues

Before submitting an issue, please:
- Search existing issues to avoid duplicates
- Use a clear title describing the problem
- Provide detailed reproduction steps
- Include necessary environment information (OS, JDK version, Spring Boot version, etc.)
- Attach relevant logs or error messages

**Issue Template**:
```markdown
### Description
Brief description of the issue

### Steps to Reproduce
1. Execute...
2. Visit...
3. See error...

### Expected Behavior
Describe what you expected to happen

### Actual Behavior
Describe what actually happened

### Environment
- OS: [e.g. macOS 13.0]
- JDK: [e.g. OpenJDK 25]
- Spring Boot: [e.g. 4.0.1]
- Goya Version: [e.g. 1.0.0]

### Additional Information
Any other information that helps understand the issue
```

#### 2. Suggesting Features

We welcome new feature suggestions! Please:
- Clearly describe the use case
- Explain why this feature adds value
- Provide design proposals or prototypes if possible

#### 3. Submitting Code

##### Prerequisites

1. **Fork the Project**
   ```bash
   # Fork the project on GitHub to your account
   ```

2. **Clone the Code**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Goya.git
   cd Goya
   ```

3. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

##### Development Standards

###### Java Code Standards

1. **Naming Conventions**
   - Package: `com.ysmjjsy.goya.{module}.{layer}`
   - Class: PascalCase
   - Method/Variable: camelCase
   - Constant: UPPER_SNAKE_CASE
   - Constant Interface: `I*Constants`
   - Enum: `*Enum`
   - Exception: `*Exception`
   - Config Class: `*AutoConfiguration`

2. **Required Annotations**
   ```java
   @Slf4j                          // Logging (required)
   @RequiredArgsConstructor        // Constructor injection
   @Transactional(rollbackFor = Exception.class)  // Transaction
   ```

3. **Comment Standards**
   - All comments in **Chinese**
   - Class comments: Description + Example + `@author` + `@since` + `@see`
   - Method comments: Description + `@param` + `@return` + `@throws`

4. **Prohibited**
   - No underscores in property names
   - No end-of-line comments
   - No sensitive information (keys, passwords) in code

###### TypeScript/Vue Standards

1. **Naming Conventions**
   - Component files: PascalCase.vue
   - Utility files: camelCase.ts
   - API functions: `xxxApi()` (e.g., `loginApi`, `getUserInfoApi`)
   - Stores: `use*Store` (e.g., `useAuthStore`)

2. **Vue Standards**
   - Prefer `<script setup>` syntax
   - Use Setup style for stores

##### Submitting Code

1. **Code Quality Check**
   ```bash
   # Java Backend
   cd Goya
   mvn clean verify
   
   # Vue Frontend
   cd Goya-Web
   pnpm lint
   pnpm typecheck
   ```

2. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: add user login functionality"
   ```

   **Commit Message Convention** (Conventional Commits):
   - `feat:` New feature
   - `fix:` Bug fix
   - `docs:` Documentation update
   - `style:` Code formatting
   - `refactor:` Refactoring
   - `perf:` Performance optimization
   - `test:` Testing
   - `chore:` Build/tooling

3. **Push to Remote**
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create Pull Request**
   - Create PR on GitHub
   - Provide clear PR description
   - Link related issues
   - Wait for code review

##### Pull Request Guidelines

**PR Title Format**:
```
type(scope): short description

Examples:
feat(security): add SMS login support
fix(cache): fix multi-level cache sync issue
docs(readme): update quick start guide
```

**PR Description Template**:
```markdown
## Change Type
- [ ] New feature
- [ ] Bug fix
- [ ] Documentation
- [ ] Refactoring
- [ ] Performance
- [ ] Testing
- [ ] Other

## Description
Detailed description of your changes

## Related Issue
Closes #123

## Testing
- [ ] Added unit tests
- [ ] Added integration tests
- [ ] Manual testing passed

## Screenshots/Recording
(if applicable)

## Checklist
- [ ] Code follows project standards
- [ ] Self-reviewed and tested
- [ ] Documentation updated
- [ ] No conflicts
```

### Development Environment Setup

#### Backend Environment

```bash
# 1. Clone repository
git clone https://github.com/GoyaDo/Goya.git
cd Goya/Goya

# 2. Install dependencies
mvn clean install -DskipTests

# 3. Start infrastructure (Docker)
cd doc/docker/docker-compose/basic
docker-compose up -d

# 4. Start application
cd ../../../../platform/platform-monolith/auth-server
mvn spring-boot:run
```

#### Frontend Environment

```bash
# 1. Enter frontend directory
cd Goya-Web

# 2. Install pnpm (if not installed)
npm install -g pnpm

# 3. Install dependencies
pnpm install

# 4. Start dev server
pnpm dev:antd
```

### Code Review Process

1. After submitting PR, maintainers will review the code
2. Make changes based on feedback
3. Once all checks pass, PR will be merged
4. Your contribution will appear in the Contributors list

### Release Process

The project follows Semantic Versioning:
- Major: Incompatible API changes
- Minor: Backward-compatible functionality additions
- Patch: Backward-compatible bug fixes

### Community

- **GitHub Discussions**: Technical discussions and Q&A
- **GitHub Issues**: Bug reports and feature requests

### License

By contributing, you agree that your contributions will be licensed under Apache License 2.0.

---

<div align="center">

**Thank you for contributing to Goya!**

**感谢您为 Goya 做出贡献！**

</div>
