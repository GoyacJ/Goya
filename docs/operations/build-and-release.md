# 构建与发布

## 本地构建
```bash
mvn -DskipTests compile
```

## 模块构建
```bash
mvn -pl platform/platform-monolith -am -DskipTests validate
```

## 发布注意项
- 发布前确认 `deploy/maven/conf/settings.xml` 中凭据来自环境变量。
- 发布前执行敏感信息检查，避免明文凭据入库。
- 仅发布当前可运行模块，骨架模块不强制补齐。
