# 配置与密钥管理

## 敏感信息规则
- 禁止在仓库内提交明文 `password/secret/token/access key`。
- 必须使用 `${ENV_VAR}` 或 `${ENV_VAR:DEFAULT}` 形式。

## 关键文件
- Docker 基础设施环境：`deploy/docker/docker-compose/basic/.env`
- Maven 仓库配置：`deploy/maven/conf/settings.xml`
- 应用环境配置：
  - `platform/platform-monolith/src/main/resources/application-dev.yml`
  - `platform/platform-monolith/src/main/resources/application-prod.yml`

## 推荐做法
- 本地使用私有环境变量文件，不入库。
- 对历史暴露凭据执行轮换。
- 模板文件保留占位符，真实值由运行环境注入。
