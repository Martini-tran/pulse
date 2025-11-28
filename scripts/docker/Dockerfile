# 多阶段构建
FROM maven:3.8.4-openjdk-8-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制pom文件
COPY pom.xml .
COPY */pom.xml ./

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY . .

# 编译打包
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:8-jre-slim

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用目录
WORKDIR /app

# 创建非root用户
RUN groupadd -r pulse && useradd -r -g pulse pulse

# 复制jar文件
COPY --from=builder /app/pulse-manager/target/pulse-manager-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R pulse:pulse /app

# 切换到非root用户
USER pulse

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]