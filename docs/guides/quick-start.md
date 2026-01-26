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

### 2. 可选软件

| 软件 | 用途 | 下载地址 |
|------|------|---------|
| IntelliJ IDEA | Java 开发 | [JetBrains](https://www.jetbrains.com/idea/) |
| VS Code | 前端开发 | [VS Code](https://code.visualstudio.com/) |
| Redis Desktop Manager | Redis 管理 | [Another Redis Desktop Manager](https://github.com/qishibo/AnotherRedisDesktopManager) |
| Navicat | 数据库管理 | [Navicat](https://www.navicat.com/) |

### 3. 验证安装

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
# 克隆代码（如果有权限）
git clone https://github.com/GoyaDo/Goya.git
cd Goya

# 或者 Fork 后克隆
git clone https://github.com/YOUR_USERNAME/Goya.git
cd Goya
```

---

## 启动基础设施

Goya 依赖以下基础设施：
- **MySQL/PostgreSQL**：数据存储
- **Redis**：缓存和 Session
- **Nacos**：服务注册和配置中心（可选）

### 方式一：使用 Docker Compose（推荐）

```bash
cd Goya/doc/docker/docker-compose/basic
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

### 方式二：手动安装

如果不使用 Docker，需要手动安装并启动这些服务。

**MySQL**：

```bash
# macOS
brew install mysql
brew services start mysql

# Ubuntu
sudo apt install mysql-server
sudo systemctl start mysql

# 创建数据库
mysql -uroot -p
CREATE DATABASE goya CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Redis**：

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu
sudo apt install redis-server
sudo systemctl start redis-server
```

---

## 后端启动

### 1. 配置数据库

编辑配置文件 `Goya/platform/platform-monolith/auth-server/src/main/resources/application-dev.yml`：

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

### 2. 初始化数据库

```bash
# 执行数据库初始化脚本（如果有）
cd Goya/doc/sql
mysql -uroot -proot goya < schema.sql
mysql -uroot -proot goya < data.sql
```

### 3. 安装依赖

```bash
cd Goya
mvn clean install -DskipTests
```

**首次构建可能需要 5-10 分钟**，Maven 会下载所有依赖。

### 4. 启动认证服务器

```bash
cd platform/platform-monolith/auth-server
mvn spring-boot:run
```

或者在 IntelliJ IDEA 中：
1. 打开 `AuthApplication.java`
2. 右键选择 `Run 'AuthApplication'`

**启动成功标志**：

```
[Goya] |- Application started successfully
[Goya] |- Auth Server is running on http://localhost:8080
```

### 5. 验证后端服务

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
cd Goya-Web

# 安装 pnpm（如果未安装）
npm install -g pnpm

# 安装依赖
pnpm install
```

### 2. 配置后端地址

编辑 `Goya-Web/apps/web-antd/.env.development`：

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

**默认账号**（如果有）：
- 用户名：`admin`
- 密码：`admin123`

---

## 第一个接口

### 1. 创建控制器

在 `auth-server` 模块中创建一个简单的 REST 接口：

```java
package com.ysmjjsy.goya.auth.server.controller;

import com.ysmjjsy.goya.component.core.response.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Hello World 控制器
 *
 * @author goya
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/hello")
public class HelloController {
    
    /**
     * Hello World
     */
    @GetMapping
    public Response<String> hello() {
        log.info("Hello World API called");
        return Response.ok("Hello Goya!");
    }
    
    /**
     * 问候
     */
    @PostMapping("/greet")
    public Response<GreetResponse> greet(@RequestBody GreetRequest request) {
        log.info("Greet API called with name: {}", request.getName());
        
        GreetResponse response = new GreetResponse();
        response.setMessage("Hello, " + request.getName() + "!");
        response.setTimestamp(System.currentTimeMillis());
        
        return Response.ok(response);
    }
    
    @Data
    public static class GreetRequest {
        private String name;
    }
    
    @Data
    public static class GreetResponse {
        private String message;
        private Long timestamp;
    }
}
```

### 2. 重启后端

```bash
# 如果使用 maven
mvn spring-boot:run

# 或者在 IDEA 中重新运行
```

### 3. 测试接口

```bash
# 测试 GET 接口
curl http://localhost:8080/api/hello

# 预期输出:
# {
#   "code": "0200",
#   "message": "操作成功",
#   "isSuccess": true,
#   "data": "Hello Goya!",
#   "timestamp": "2025-01-24 12:00:00"
# }

# 测试 POST 接口
curl -X POST http://localhost:8080/api/hello/greet \
  -H "Content-Type: application/json" \
  -d '{"name":"Goya"}'

# 预期输出:
# {
#   "code": "0200",
#   "message": "操作成功",
#   "isSuccess": true,
#   "data": {
#     "message": "Hello, Goya!",
#     "timestamp": 1737702000000
#   },
#   "timestamp": "2025-01-24 12:00:00"
# }
```

---

## 常见问题

### 1. 端口冲突

**问题**：启动时提示端口已被占用

```
Port 8080 is already in use
```

**解决方案**：

方案 A - 修改端口（`application-dev.yml`）：

```yaml
server:
  port: 8081
```

方案 B - 杀掉占用端口的进程：

```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### 2. Maven 依赖下载失败

**问题**：Maven 下载依赖超时

**解决方案**：配置国内镜像（`~/.m2/settings.xml`）：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### 3. 数据库连接失败

**问题**：无法连接数据库

```
Could not open JDBC Connection for transaction
```

**解决方案**：

1. 检查数据库是否启动：

```bash
docker-compose ps
# 或
mysql -h127.0.0.1 -uroot -proot
```

2. 检查配置文件中的数据库地址、用户名、密码是否正确

3. 检查数据库是否创建：

```bash
mysql -uroot -proot -e "SHOW DATABASES;"
```

### 4. Redis 连接失败

**问题**：无法连接 Redis

```
Unable to connect to Redis
```

**解决方案**：

```bash
# 检查 Redis 是否启动
redis-cli ping

# 如果未启动，启动 Redis
docker-compose up -d redis
# 或
brew services start redis
```

### 5. 前端启动失败

**问题**：pnpm install 失败

**解决方案**：

```bash
# 清除缓存
pnpm store prune

# 重新安装
pnpm install

# 如果还是失败，使用 npm
npm install
```

### 6. Node Sass 编译错误

**问题**：node-sass 或 sass 相关错误

**解决方案**：

```bash
# 使用 dart-sass 替代
pnpm add -D sass
```

---

## 开发工具配置

### IntelliJ IDEA

1. **安装插件**：
   - Lombok Plugin
   - MyBatis X
   - Spring Tools
   - GenerateAllSetter

2. **配置 Annotation Processing**：
   - `Preferences` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - 勾选 `Enable annotation processing`

3. **配置代码风格**：
   - 导入 `Goya/doc/ide/intellij-code-style.xml`
   - `Preferences` → `Editor` → `Code Style` → `Import Scheme`

### VS Code

1. **安装插件**：
   - Volar (Vue 3)
   - TypeScript Vue Plugin
   - ESLint
   - Prettier
   - Tailwind CSS IntelliSense

2. **配置文件**（`.vscode/settings.json`）：

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "eslint.validate": ["javascript", "typescript", "vue"],
  "volar.takeOverMode.enabled": true
}
```

---

## 下一步

现在您已经成功运行了 Goya 项目！接下来可以：

- [开发指南](./development.md) - 了解详细的开发规范和最佳实践
- [架构设计](../architecture/overview.md) - 深入理解 Goya 的架构设计
- [模块详解](../architecture/modules.md) - 学习各个模块的功能
- [部署指南](./deployment.md) - 学习如何部署到生产环境

## 获取帮助

- **GitHub Issues**: https://github.com/GoyaDo/Goya/issues
- **官方文档**: https://www.ysmjjsy.com
- **社区讨论**: GitHub Discussions
