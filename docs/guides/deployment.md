# 部署指南 | Deployment Guide

本指南介绍如何将 Goya 应用部署到不同环境。

## 环境说明

| 环境 | 用途 | 配置文件 |
|------|------|---------|
| dev | 本地开发 | application-dev.yml |
| test | 测试环境 | application-test.yml |
| prod | 生产环境 | application-prod.yml |

## 打包构建

### 后端打包

```bash
# 跳过测试打包
mvn clean package -DskipTests -P prod

# 包含测试打包
mvn clean package -P prod

# 输出位置
# platform/platform-monolith/target/platform-monolith-1.0.0.jar
```

### 前端打包

```bash
cd goya-web-ui

# Ant Design Vue 版本
pnpm build:antd

# Element Plus 版本
pnpm build:ele

# 输出位置
# apps/web-antd/dist/
```

## 单体应用部署

### 方式一：JAR 直接运行

```bash
# 启动应用
java -jar platform-monolith-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080

# 后台运行
nohup java -jar platform-monolith-1.0.0.jar \
  --spring.profiles.active=prod \
  > app.log 2>&1 &

# 查看日志
tail -f app.log
```

### 方式二：Systemd 服务

创建服务文件 `/etc/systemd/system/goya-auth.service`：

```ini
[Unit]
Description=Goya Auth Server
After=network.target

[Service]
Type=simple
User=goya
WorkingDirectory=/opt/goya
ExecStart=/usr/bin/java -jar /opt/goya/platform-monolith.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

管理服务：

```bash
# 启动服务
sudo systemctl start goya-auth

# 开机自启
sudo systemctl enable goya-auth

# 查看状态
sudo systemctl status goya-auth
```

## Docker 部署

### Dockerfile

```dockerfile
FROM eclipse-temurin:25-jre-alpine

LABEL maintainer="goya@ysmjjsy.com"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  auth-server:
    image: goya/auth-server:1.0.0
    container_name: goya-auth
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/goya
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
      - SPRING_DATA_REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
    restart: unless-stopped
    networks:
      - goya-network

  mysql:
    image: mysql:8.0
    container_name: goya-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=goya
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - goya-network

  redis:
    image: redis:7-alpine
    container_name: goya-redis
    volumes:
      - redis-data:/data
    networks:
      - goya-network

volumes:
  mysql-data:
  redis-data:

networks:
  goya-network:
    driver: bridge
```

启动：

```bash
docker-compose up -d
```

## Kubernetes 部署

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: goya-auth
  namespace: goya
spec:
  replicas: 3
  selector:
    matchLabels:
      app: goya-auth
  template:
    metadata:
      labels:
        app: goya-auth
    spec:
      containers:
      - name: auth-server
        image: registry.example.com/goya/auth-server:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: goya-auth-service
  namespace: goya
spec:
  selector:
    app: goya-auth
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

### 部署

```bash
# 创建命名空间
kubectl create namespace goya

# 应用配置
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 查看状态
kubectl get pods -n goya
kubectl get svc -n goya
```

## 前端部署

### Nginx 配置

```nginx
server {
    listen 80;
    server_name example.com;

    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://auth-server:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## 生产优化

### JVM 参数

```bash
java -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof \
  -Dspring.profiles.active=prod \
  -jar app.jar
```

### 数据库优化

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 监控

### Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## 下一步

- [架构设计](../architecture/overview.md)
- [开发指南](./development.md)
- [快速开始](./quick-start.md)
