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
cd Goya

# 跳过测试打包
mvn clean package -DskipTests -P prod

# 包含测试打包
mvn clean package -P prod

# 输出位置
# platform/platform-monolith/auth-server/target/auth-server-1.0.0.jar
```

### 前端打包

```bash
cd Goya-Web

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
java -jar auth-server-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080

# 后台运行
nohup java -jar auth-server-1.0.0.jar \
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
ExecStart=/usr/bin/java -jar /opt/goya/auth-server.jar --spring.profiles.active=prod
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

# 查看日志
sudo journalctl -u goya-auth -f
```

## Docker 部署

### Dockerfile

创建 `Dockerfile`：

```dockerfile
FROM eclipse-temurin:25-jre-alpine

LABEL maintainer="goya@ysmjjsy.com"

WORKDIR /app

# 复制 JAR 文件
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O- http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

### 构建镜像

```bash
# 构建镜像
docker build -t goya/auth-server:1.0.0 .

# 推送到私有仓库
docker tag goya/auth-server:1.0.0 registry.example.com/goya/auth-server:1.0.0
docker push registry.example.com/goya/auth-server:1.0.0
```

### Docker Compose

创建 `docker-compose.yml`：

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

创建 `k8s/deployment.yaml`：

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
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: goya-secrets
              key: database-url
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

创建 `k8s/service.yaml`：

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

### Ingress

创建 `k8s/ingress.yaml`：

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: goya-ingress
  namespace: goya
  annotations:
    kubernetes.io/ingress.class: "nginx"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - api.example.com
    secretName: goya-tls
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: goya-auth-service
            port:
              number: 80
```

### 部署

```bash
# 创建命名空间
kubectl create namespace goya

# 创建 Secret
kubectl create secret generic goya-secrets \
  --from-literal=database-url=jdbc:mysql://mysql:3306/goya \
  --from-literal=database-username=root \
  --from-literal=database-password=root \
  -n goya

# 应用配置
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# 查看状态
kubectl get pods -n goya
kubectl get svc -n goya
kubectl get ingress -n goya

# 查看日志
kubectl logs -f deployment/goya-auth -n goya
```

## 前端部署

### Nginx

创建 `nginx.conf`：

```nginx
server {
    listen 80;
    server_name example.com;

    root /usr/share/nginx/html;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    # SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://auth-server:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Docker 部署前端

创建 `Dockerfile.frontend`：

```dockerfile
FROM node:20-alpine as builder

WORKDIR /app

COPY package.json pnpm-lock.yaml ./
RUN npm install -g pnpm && pnpm install

COPY . .
RUN pnpm build:antd

FROM nginx:alpine

COPY --from=builder /app/apps/web-antd/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

构建和运行：

```bash
docker build -f Dockerfile.frontend -t goya/web:1.0.0 .
docker run -d -p 80:80 goya/web:1.0.0
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

### Redis 优化

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          min-idle: 5
          max-idle: 10
          max-active: 20
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

### Prometheus

```yaml
scrape_configs:
  - job_name: 'goya-auth'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana

导入 Spring Boot Dashboard: ID `11378`

## 故障排查

### 查看日志

```bash
# Docker
docker logs -f goya-auth

# Kubernetes
kubectl logs -f deployment/goya-auth -n goya

# Systemd
journalctl -u goya-auth -f
```

### 内存分析

```bash
# 生成堆转储
jmap -dump:live,format=b,file=heapdump.hprof <pid>

# 分析堆转储
jhat heapdump.hprof
```

## 下一步

- [架构设计](../architecture/overview.md)
- [开发指南](./development.md)
- [快速开始](./quick-start.md)
