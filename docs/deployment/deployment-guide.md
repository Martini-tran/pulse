# 部署指南

## 环境要求

### 基础环境
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 推荐配置
- CPU: 4核心以上
- 内存: 8GB以上
- 磁盘: 100GB以上

## 本地开发部署

### 1. 环境准备

```bash
# 安装JDK 1.8
java -version

# 安装Maven
mvn -version

# 启动MySQL服务
# 创建数据库
CREATE DATABASE pulse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 启动Redis服务
redis-server
```

### 2. 项目构建

```bash
# 克隆项目
git clone <repository-url>
cd pulse

# 编译项目
mvn clean compile

# 打包项目
mvn clean package -DskipTests
```

### 3. 数据库初始化

```bash
# 执行SQL脚本
mysql -u root -p pulse < doc/pulse.sql
```

### 4. 配置文件修改

修改各模块的 `application.yml` 配置文件：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pulse?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password
    
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 5. 启动服务

```bash
# 启动管理服务
cd pulse-manager
mvn spring-boot:run

# 启动应用服务
cd pulse-app
mvn spring-boot:run
```

## Docker 部署

### 1. 构建镜像

```bash
# 构建基础镜像
docker build -t pulse-base .

# 构建各服务镜像
docker build -t pulse-manager ./pulse-manager
docker build -t pulse-app ./pulse-app
```

### 2. 使用Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f pulse-manager
```

## Kubernetes 部署

### 1. 创建命名空间

```bash
kubectl create namespace pulse
```

### 2. 部署配置

```bash
# 部署ConfigMap
kubectl apply -f k8s/configmap.yaml

# 部署Secret
kubectl apply -f k8s/secret.yaml

# 部署服务
kubectl apply -f k8s/
```

### 3. 验证部署

```bash
# 查看Pod状态
kubectl get pods -n pulse

# 查看服务状态
kubectl get svc -n pulse

# 查看日志
kubectl logs -f deployment/pulse-manager -n pulse
```

## 生产环境部署

### 1. 环境规划

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   负载均衡器     │    │   应用服务器     │    │   数据库服务器   │
│   (Nginx)       │    │   (Docker)      │    │   (MySQL)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   缓存服务器     │
                    │   (Redis)       │
                    └─────────────────┘
```

### 2. 性能优化

```yaml
# JVM参数优化
JAVA_OPTS: >
  -Xms2g -Xmx4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/logs/heapdump.hprof
```

### 3. 监控配置

```yaml
# Prometheus监控
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

## 故障排查

### 常见问题

1. **服务启动失败**
   - 检查端口占用
   - 检查配置文件
   - 查看启动日志

2. **数据库连接失败**
   - 检查数据库服务状态
   - 验证连接参数
   - 检查网络连通性

3. **Redis连接失败**
   - 检查Redis服务状态
   - 验证连接参数
   - 检查防火墙设置

### 日志查看

```bash
# 查看应用日志
tail -f logs/pulse-manager.log

# 查看错误日志
grep ERROR logs/pulse-manager.log

# 查看Docker容器日志
docker logs -f pulse-manager
```

## 备份与恢复

### 数据库备份

```bash
# 备份数据库
mysqldump -u root -p pulse > backup/pulse_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p pulse < backup/pulse_20240101.sql
```

### Redis备份

```bash
# 备份Redis
redis-cli BGSAVE

# 复制RDB文件
cp /var/lib/redis/dump.rdb backup/redis_$(date +%Y%m%d).rdb