# 文档结构说明 | Documentation Structure

## 文档组织原则

1. **README.md**：项目主文档，所有文档的入口
2. **.cursor/**：Cursor AI 工具目录，供 Cursor 使用，不包含额外的 README
3. **docs/**：项目文档目录，面向开发者

## 文档结构

### 项目主文档

- **README.md**：项目主文档
  - 项目概况、技术栈、核心特性
  - 项目结构、快速开始
  - 核心模块详解
  - 文档导航
  - 开发规范说明

### Cursor 工具目录（.cursor/）

**供 Cursor AI 使用，不包含额外的 README**

```
.cursor/
├── AI_ASSISTANT_GUIDE.md         # AI 助手使用指南（AI 助手必读）
├── rules/                        # Rules（.mdc 格式）
│   ├── ai-assistant-guide.mdc    # AI 助手开发规范
│   ├── ai-workflow-enforcement.mdc # AI 工作流强制执行规则
│   ├── development-workflow.mdc  # 开发工作流规范
│   ├── pre-development-checklist.mdc # 开发前检查清单
│   └── post-development-checklist.mdc # 开发后检查清单
├── skills/                       # Skills（SKILL.md 格式）
│   └── goya-development-workflow/
│       └── SKILL.md
├── hooks/                        # Git Hooks
│   ├── pre-commit.sh
│   ├── commit-msg.sh
│   └── README.md                 # Hooks 使用说明（简化版）
└── hooks.json                    # Cursor Hooks 配置
```

### 项目文档目录（docs/）

```
docs/
├── SUMMARY.md                    # 文档总览和导航
├── architecture/                 # 架构文档
│   ├── overview.md               # 架构概览
│   ├── modules.md                # 模块详解（概览）
│   ├── modules-detailed.md       # 模块详细文档（技术细节）
│   └── design-patterns.md        # 设计模式
├── guides/                       # 开发指南
│   ├── quick-start.md            # 快速开始
│   ├── development.md            # 开发规范
│   ├── deployment.md             # 部署指南
│   └── api-reference.md          # API 参考
├── requirements/                 # 需求文档
│   ├── product-requirements.md   # PRD
│   ├── technical-requirements.md # TRD
│   └── features/                 # 功能需求
│       ├── auth-system.md        # 认证授权
│       ├── ai-integration.md     # AI 集成
│       └── multi-tenant.md       # 多租户
└── progress/                     # 开发进度
    ├── roadmap.md                # 路线图
    ├── milestones.md             # 里程碑
    ├── changelog.md              # 更新日志
    └── PROGRESS_TEMPLATE.md      # 模块进度文档模板
```

## 文档层次

### 第一层：项目主文档
- **README.md**：项目入口，包含所有重要信息的导航

### 第二层：核心文档
- **docs/SUMMARY.md**：文档总览和导航
- **docs/architecture/overview.md**：架构设计
- **docs/guides/development.md**：开发规范

### 第三层：详细文档
- **docs/architecture/modules-detailed.md**：模块技术细节
- **docs/guides/api-reference.md**：API 参考
- **docs/progress/changelog.md**：变更记录

### 第四层：AI 开发规范
- **.cursor/AI_ASSISTANT_GUIDE.md**：AI 助手使用指南（AI 助手必读）
- **.cursor/rules/ai-assistant-guide.mdc**：AI 助手开发规范（Rules 格式）
- **.cursor/rules/development-workflow.mdc**：开发工作流规范

## 文档使用指南

### 对于开发者

1. **开始开发前**：
   - 阅读 README.md（项目主文档）
   - 阅读 docs/architecture/overview.md
   - 阅读 docs/guides/development.md

2. **使用 Cursor AI 开发**：
   - 阅读 .cursor/AI_ASSISTANT_GUIDE.md
   - 激活相关 Rules
   - 使用相关 Skills

3. **开发完成后**：
   - 更新 docs/progress/changelog.md
   - 更新相关文档

### 对于 AI 助手

1. **必须阅读**：
   - README.md（项目主文档）
   - .cursor/AI_ASSISTANT_GUIDE.md

2. **必须激活**：
   - .cursor/rules/ai-assistant-guide.mdc
   - .cursor/rules/development-workflow.mdc

3. **必须使用**：
   - goya-development-workflow Skill

## 已删除的冗余文档

以下文档已被删除，内容已整合到其他文档中：

- `.cursor/README.md` - 内容整合到 README.md
- `.cursor/QUICK_START.md` - 内容整合到 AI_ASSISTANT_GUIDE.md
- `.cursor/INSTALLATION.md` - 内容整合到 AI_ASSISTANT_GUIDE.md
- `.cursor/rules/README.md` - 不需要，Rules 目录供 Cursor 使用
- `.cursor/skills/README.md` - 不需要，Skills 目录供 Cursor 使用
- `docs/progress/DEVELOPMENT_WORKFLOW.md` - 与 .cursor/rules/development-workflow.mdc 重复
- `docs/guides/ai-development-guide.md` - 与 .cursor/AI_ASSISTANT_GUIDE.md 重复

## 文档维护原则

1. **单一来源**：每个主题只有一个权威文档
2. **层次清晰**：文档层次分明，避免重复
3. **易于查找**：通过 README.md 可以找到所有文档
4. **及时更新**：代码变更后必须同步更新文档
