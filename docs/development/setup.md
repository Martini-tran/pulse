# 开发环境搭建

## 环境要求

### 必需软件
- **JDK**: 1.8 或更高版本
- **Maven**: 3.6.0 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 5.0 或更高版本
- **Git**: 最新版本

### 推荐IDE
- IntelliJ IDEA (推荐)
- Eclipse
- Visual Studio Code

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd pulse
```

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE pulse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'pulse'@'localhost' IDENTIFIED BY 'pulse123';
GRANT ALL PRIVILEGES ON pulse.* TO 'pulse'@'localhost';
FLUSH PRIVILEGES;

-- 导入初始数据
USE pulse;
SOURCE doc/pulse.sql;
```

### 3. Redis配置

```bash
# 启动Redis服务
redis-server

# 验证Redis连接
redis-cli ping
```

### 4. 项目配置

复制配置文件模板并修改：

```bash
# 复制配置文件
cp pulse-manager/src/main/resources/application-dev.yml.template \
   pulse-manager/src/main/resources/application-dev.yml

cp pulse-app/src/main/resources/application-dev.yml.template \
   pulse-app/src/main/resources/application-dev.yml
```

修改数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pulse?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: pulse
    password: pulse123
    
  redis:
    host: localhost
    port: 6379
    password: # 如果有密码请填写
```

### 5. 编译项目

```bash
# 安装依赖并编译
mvn clean compile

# 跳过测试打包
mvn clean package -DskipTests
```

### 6. 启动服务

```bash
# 启动管理服务 (端口: 8080)
cd pulse-manager
mvn spring-boot:run

# 新开终端，启动应用服务 (端口: 8081)
cd pulse-app
mvn spring-boot:run
```

## IDE配置

### IntelliJ IDEA

1. **导入项目**
   - File → Open → 选择项目根目录
   - 选择 "Import project from external model" → Maven

2. **配置JDK**
   - File → Project Structure → Project Settings → Project
   - 设置 Project SDK 为 JDK 1.8

3. **配置Maven**
   - File → Settings → Build Tools → Maven
   - 设置 Maven home directory
   - 设置 User settings file

4. **安装推荐插件**
   - Lombok Plugin
   - MyBatis Plugin
   - Spring Boot Helper

### 代码格式化

导入项目代码格式化配置：

```bash
# 复制代码格式化配置
cp .idea/codeStyles/Project.xml.template .idea/codeStyles/Project.xml
```

## 开发规范

### 代码规范

1. **命名规范**
   - 类名：大驼峰命名法 (PascalCase)
   - 方法名：小驼峰命名法 (camelCase)
   - 常量：全大写下划线分隔 (UPPER_SNAKE_CASE)
   - 包名：全小写，点分隔

2. **注释规范**
   ```java
   /**
    * 用户服务接口
    * 
    * @author your-name
    * @since 1.0.0
    */
   public interface UserService {
       
       /**
        * 根据用户ID获取用户信息
        * 
        * @param userId 用户ID
        * @return 用户信息
        */
       UserDto getUserById(Long userId);
   }
   ```

3. **异常处理**
   ```java
   try {
       // 业务逻辑
   } catch (BusinessException e) {
       log.error("业务异常: {}", e.getMessage(), e);
       throw e;
   } catch (Exception e) {
       log.error("系统异常: {}", e.getMessage(), e);
       throw new SystemException("系统异常", e);
   }
   ```

### Git规范

1. **分支命名**
   - feature/功能名称
   - bugfix/问题描述
   - hotfix/紧急修复

2. **提交信息**
   ```
   type(scope): subject
   
   body
   
   footer
   ```
   
   类型说明：
   - feat: 新功能
   - fix: 修复bug
   - docs: 文档更新
   - style: 代码格式调整
   - refactor: 重构
   - test: 测试相关
   - chore: 构建过程或辅助工具的变动

### 数据库规范

1. **表命名**
   - 使用下划线分隔
   - 表名前缀统一 (如: pulse_user, pulse_motion)

2. **字段命名**
   - 使用下划线分隔
   - 布尔字段以 is_ 开头
   - 时间字段以 _time 或 _at 结尾

3. **索引规范**
   - 主键索引：pk_表名
   - 唯一索引：uk_表名_字段名
   - 普通索引：idx_表名_字段名

## 调试技巧

### 日志配置

```yaml
logging:
  level:
    com.tran.pulse: DEBUG
    org.springframework.web: DEBUG
    org.mybatis: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 常用调试命令

```bash
# 查看端口占用
netstat -tulpn | grep :8080

# 查看Java进程
jps -l

# 查看JVM参数
jinfo -flags <pid>

# 生成线程dump
jstack <pid> > thread_dump.txt

# 生成内存dump
jmap -dump:format=b,file=heap_dump.hprof <pid>
```

## 测试

### 单元测试

```java
@SpringBootTest
@Transactional
@Rollback
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testGetUserById() {
        // given
        Long userId = 1L;
        
        // when
        UserDto user = userService.getUserById(userId);
        
        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
    }
}
```

### 接口测试

使用Postman或curl进行接口测试：

```bash
# 用户登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## 常见问题

### 1. 端口冲突
```bash
# 查找占用端口的进程
lsof -i :8080

# 杀死进程
kill -9 <pid>
```

### 2. Maven依赖问题
```bash
# 清理并重新下载依赖
mvn clean
mvn dependency:purge-local-repository
mvn compile
```

### 3. 数据库连接问题
- 检查数据库服务是否启动
- 验证连接参数是否正确
- 检查防火墙设置

### 4. Redis连接问题
- 检查Redis服务是否启动
- 验证连接参数是否正确
- 检查Redis配置文件