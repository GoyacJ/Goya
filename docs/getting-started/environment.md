# 环境要求

## 必需环境
- JDK 25+
- Maven 3.9+
- Docker（可选）

## 本仓库约束
- 不新增测试文件（`src/test/**`）。
- 不新增测试依赖。
- 验证以 `-DskipTests` 构建命令为准。

## 关键配置文件
- Maven 配置：`deploy/maven/conf/settings.xml`
- Docker 配置：`deploy/docker/docker-compose/basic/.env`
- 应用配置：
  - `platform/platform-monolith/src/main/resources/application.yml`
  - `platform/platform-monolith/src/main/resources/application-dev.yml`
  - `platform/platform-monolith/src/main/resources/application-prod.yml`
