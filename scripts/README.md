# Scripts 目录说明

本目录包含了 Pulse 项目的部署脚本和配置文件，支持多种部署方式。

## 目录结构

```
scripts/
├── docker/                 # Docker 相关配置
│   ├── Dockerfile          # 通用 Dockerfile
│   ├── docker-compose.yml  # Docker Compose 配置
│   └── nginx/              # Nginx 配置
│       └── nginx.conf      # Nginx 主配置文件
├── k8s/                    # Kubernetes 部署配置
│   ├── namespace.yaml      # 命名空间配置
│   ├── configmap.yaml      # 配置映射
│   ├── secret.yaml         # 密钥配置
│   └── mysql-deployment.yaml # MySQL 部署配置
├── ci-cd/                  # CI/CD 流水线配置
│   ├── Jenkinsfile         # Jenkins 流水线
│   └── gitlab-ci.yml       # GitLab CI/CD 配置
├── sql/                    # 数据库脚本
│   └── init.sql            # 数据库初始化脚本
└── README.md               # 本说明文件
```

## 使用方式

### 1. Docker 部署

#### 快速启动
```bash
# 进入docker目录
cd scripts/docker

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 单独构建镜像
```bash
# 构建管理服务镜像
docker build -t pulse-manager -f scripts/docker/Dockerfile .

# 运行容器
docker run -d --name pulse-manager -p 8080:8080 pulse-manager
```

### 2. Kubernetes 部署

#### 创建命名空间和配置
```bash
# 创建命名空间
kubectl apply -f scripts/k8s/namespace.yaml

# 创建配置和密钥
kubectl apply -f scripts/k8s/configmap.yaml
kubectl apply -f scripts/k8s/secret.yaml

# 部署MySQL
kubectl apply -f scripts/k8s/mysql-deployment.yaml
```

#### 验证部署
```bash
# 查看Pod状态
kubectl get pods -n pulse

# 查看服务状态
kubectl get svc -n pulse

# 查看配置
kubectl get configmap -n pulse
kubectl get secret -n pulse
```

### 3. CI/CD 配置

#### Jenkins 流水线
1. 在Jenkins中创建新的Pipeline项目
2. 配置Git仓库地址
3. 指定Jenkinsfile路径：`scripts/ci-cd/Jenkinsfile`
4. 配置必要的凭据：
   - `docker-registry-credentials`: Docker镜像仓库凭据
   - `k8s-dev-config`: 开发环境K8s配置
   - `k8s-staging-config`: 预发布环境K8s配置
   - `k8s-prod-config`: 生产环境K8s配置

#### GitLab CI/CD
1. 将 `scripts/ci-cd/gitlab-ci.yml` 复制到项目根目录并重命名为 `.gitlab-ci.yml`
2. 在GitLab项目设置中配置以下变量：
   - `KUBE_CONFIG_DEV`: 开发环境K8s配置（base64编码）
   - `KUBE_CONFIG_STAGING`: 预发布环境K8s配置（base64编码）
   - `KUBE_CONFIG_PROD`: 生产环境K8s配置（base64编码）

### 4. 数据库初始化

#### 本地环境
```bash
# 连接MySQL
mysql -u root -p

# 执行初始化脚本
source scripts/sql/init.sql
```

#### Docker环境
```bash
# 复制SQL文件到容器
docker cp scripts/sql/init.sql pulse-mysql:/tmp/

# 进入容器执行
docker exec -it pulse-mysql mysql -u root -p pulse < /tmp/init.sql
```

## 配置说明

### Docker配置

#### 环境变量
- `SPRING_PROFILES_ACTIVE`: Spring激活的配置文件
- `SPRING_DATASOURCE_URL`: 数据库连接URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `SPRING_REDIS_HOST`: Redis主机地址
- `SPRING_REDIS_PORT`: Redis端口

#### 端口映射
- `8080`: 管理服务端口
- `8081`: 应用服务端口
- `3306`: MySQL数据库端口
- `6379`: Redis缓存端口
- `80/443`: Nginx代理端口

### Kubernetes配置

#### 资源配置
- CPU请求：250m，限制：500m
- 内存请求：512Mi，限制：1Gi
- 存储：MySQL使用20Gi持久化存储

#### 服务发现
- `mysql-service`: MySQL数据库服务
- `redis-service`: Redis缓存服务
- `pulse-manager-service`: 管理服务
- `pulse-app-service`: 应用服务

### 安全配置

#### 密钥管理
所有敏感信息都通过Kubernetes Secret管理：
- 数据库密码
- Redis密码
- JWT密钥

#### 网络安全
- 使用非root用户运行容器
- 配置健康检查
- 设置资源限制

## 监控和日志

### 健康检查
- HTTP健康检查端点：`/actuator/health`
- 检查间隔：30秒
- 超时时间：3秒
- 重试次数：3次

### 日志收集
- 容器日志自动收集
- 日志轮转配置
- 支持ELK Stack集成

### 监控指标
- Prometheus指标导出
- JVM性能监控
- 应用业务指标

## 故障排查

### 常见问题

1. **容器启动失败**
   ```bash
   # 查看容器日志
   docker logs container-name
   
   # 查看Pod日志
   kubectl logs pod-name -n pulse
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库服务状态
   kubectl get svc mysql-service -n pulse
   
   # 测试数据库连接
   kubectl exec -it mysql-pod -n pulse -- mysql -u pulse -p
   ```

3. **服务间通信问题**
   ```bash
   # 检查服务发现
   kubectl get endpoints -n pulse
   
   # 测试网络连通性
   kubectl exec -it pod-name -n pulse -- ping service-name
   ```

### 性能优化

1. **JVM参数调优**
   - 根据容器内存限制调整堆大小
   - 使用G1垃圾收集器
   - 启用JVM监控

2. **数据库优化**
   - 配置连接池参数
   - 启用查询缓存
   - 定期分析慢查询

3. **缓存优化**
   - 合理设置缓存过期时间
   - 监控缓存命中率
   - 配置Redis集群

## 版本管理

### 镜像标签策略
- `latest`: 最新稳定版本
- `v1.0.0`: 语义化版本号
- `commit-sha`: 基于Git提交的版本

### 回滚策略
```bash
# Kubernetes回滚
kubectl rollout undo deployment/pulse-manager-deployment -n pulse

# Docker回滚
docker-compose down
docker-compose up -d --scale pulse-manager=0
docker-compose up -d
```

## 扩展说明

### 水平扩展
```bash
# Kubernetes扩展
kubectl scale deployment pulse-manager-deployment --replicas=3 -n pulse

# Docker Compose扩展
docker-compose up -d --scale pulse-manager=3
```

### 添加新服务
1. 创建新的Dockerfile
2. 更新docker-compose.yml
3. 创建Kubernetes部署文件
4. 更新CI/CD流水线

本脚本集合为Pulse项目提供了完整的部署和运维支持，可根据实际需求进行调整和扩展。