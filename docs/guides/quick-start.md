# 快速开始 | Quick Start

本指南将帮助您快速搭建 Goya 开发环境并运行第一个应用。

## 环境准备

### 1. 必需软件

| 软件 | 版本要求 | 下载地址 |
|------|---------|---------|
| JDK | 25+ | [OpenJDK](https://adoptium.net/) |
| Maven | 3.9+ | [Apache Maven](https://maven.apache.org/) |
| Node.js | 20+ | [Node.js](https://nodejs.org/) |
| pnpm | 10+ | [pnpm](https://pnpm.io/) |
| Docker | 最新 | [Docker Desktop](https://www.docker.com/) |
| Git | 最新 | [Git](https://git-scm.com/) |

### 2. 验证安装

```bash
# 验证 JDK
java -version
# 预期输出: openjdk version "25" ...

# 验证 Maven
mvn -version
# 预期输出: Apache Maven 3.9.x ...

# 验证 Node.js
node -version
# 预期输出: v20.x.x

# 验证 pnpm
pnpm -version
# 预期输出: 10.x.x

# 验证 Docker
docker -version
# 预期输出: Docker version ...
```

---

## 获取代码

```bash
# 克隆代码
git clone https://github.com/GoyaDo/Goya.git
cd Goya
```

---

## 启动基础设施

Goya 依赖以下基础设施：
- **MySQL/PostgreSQL**：数据存储
- **Redis**：缓存和 Session
- **Nacos**：服务注册和配置中心（可选）

### 使用 Docker Compose（推荐）

```bash
cd deploy/docker/docker-compose/basic
docker-compose up -d
```

Docker Compose 会自动启动：
- MySQL 8.0（端口 3306）
- Redis 7.x（端口 6379）
- Nacos 3.1.1（端口 8848）
- MongoDB（端口 27017，可选）

**验证服务**：

```bash
# 查看容器状态
docker-compose ps

# 测试 MySQL 连接
mysql -h127.0.0.1 -uroot -proot

# 测试 Redis 连接
redis-cli ping
# 预期输出: PONG

# 访问 Nacos 控制台
open http://localhost:8848/nacos
# 默认账号/密码: nacos/nacos
```

---

## 后端启动

### 1. 配置数据库

编辑配置文件 `platform/platform-monolith/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/goya?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果 Redis 设置了密码
```

### 2. 安装依赖

```bash
mvn clean install -DskipTests
```

**首次构建可能需要 5-10 分钟**，Maven 会下载所有依赖。

### 3. 启动认证服务器

```bash
cd platform/platform-monolith
mvn spring-boot:run
```

**启动成功标志**：

```
[Goya] |- Application started successfully
[Goya] |- Auth Server is running on http://localhost:8080
```

### 4. 验证后端服务

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# OIDC Discovery
curl http://localhost:8080/.well-known/openid-configuration

# 访问 Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 前端启动

### 1. 安装依赖

```bash
cd goya-web-ui

# 安装 pnpm（如果未安装）
npm install -g pnpm

# 安装依赖
pnpm install
```

### 2. 配置后端地址

编辑 `goya-web-ui/apps/web-antd/.env.development`：

```env
# 后端 API 地址
VITE_API_URL=http://localhost:8080

# 是否开启 Mock
VITE_ENABLE_MOCK=false
```

### 3. 启动开发服务器

```bash
# Ant Design Vue 版本
pnpm dev:antd

# 或 Element Plus 版本
pnpm dev:ele

# 或 Naive UI 版本
pnpm dev:naive
```

**启动成功标志**：

```
  ➜  Local:   http://localhost:5555/
  ➜  Network: http://192.168.x.x:5555/
```

### 4. 访问前端

打开浏览器访问：`http://localhost:5555`

---

## 项目结构

```
Goya/
├── bom/                       # 依赖版本管理
├── component/                 # 公共组件
│   ├── component-framework/   # 框架基础（10个子模块）
│   ├── component-redis/       # Redis 实现
│   ├── component-kafka/       # Kafka 消息
│   ├── component-rabbitmq/    # RabbitMQ 消息
│   ├── component-mybatisplus/ # MyBatis Plus
│   ├── component-captcha/     # 验证码
│   ├── component-security/    # 安全模块（4个子模块）
│   ├── component-social/      # 社交登录
│   ├── component-oss-aliyun/  # 阿里云 OSS
│   ├── component-oss-s3/      # AWS S3
│   └── component-oss-minio/   # MinIO
├── ai/                        # AI 模块
├── platform/                  # 平台应用
├── cloud/                     # 云原生支持
├── deploy/                    # 部署配置
│   ├── docker/                # Docker Compose
│   └── maven/                 # Maven 配置
├── docs/                      # 项目文档
└── goya-web-ui/               # Vue 3 前端
```

---

## 常见问题

### 1. 端口冲突

**问题**：启动时提示端口已被占用

**解决方案**：修改端口或杀掉占用端口的进程

```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9
```

### 2. Maven 依赖下载失败

**问题**：Maven 下载依赖超时

**解决方案**：配置国内镜像，使用 `deploy/maven/conf/settings.xml`

### 3. 数据库连接失败

**问题**：无法连接数据库

**解决方案**：检查数据库是否启动

```bash
docker-compose ps
```

### 4. Redis 连接失败

**问题**：无法连接 Redis

**解决方案**：

```bash
redis-cli ping
# 如果未启动
docker-compose up -d redis
```

---

## 下一步

- [开发指南](./development.md) - 了解详细的开发规范和最佳实践
- [架构设计](../architecture/overview.md) - 深入理解 Goya 的架构设计
- [模块详解](../architecture/modules.md) - 学习各个模块的功能
- [部署指南](./deployment.md) - 学习如何部署到生产环境

## 获取帮助

- **GitHub Issues**: https://github.com/GoyaDo/Goya/issues
- **官方文档**: https://www.ysmjjsy.com
