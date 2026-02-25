# 快速开始

## 1. 编译工程（跳过测试）
```bash
mvn -DskipTests compile
```

## 2. 启动单体应用
```bash
cd platform/platform-monolith
mvn spring-boot:run
```

默认访问地址：`http://localhost:8101`

## 3. 启动基础设施（可选）
```bash
cd deploy/docker/docker-compose/basic
docker-compose up -d
```
