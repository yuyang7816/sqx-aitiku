基于 Spring Boot 2.6.11 和 Java 8 的企业级应用开发框架，采用多模块 Maven 架构，提供完整的 RBAC 权限管理、多渠道支付集成、文件存储、实时通信等功能模块。

## 技术栈

**核心框架**：Spring Boot 2.6.11、Java 8

**持久层**：MyBatis Plus 3.5.3.2、MySQL 8.0+

**缓存/分布式锁**：Redis 5.0+、Redisson 3.18.0

**安全认证**：Apache Shiro 1.4.0、JWT

**API 文档**：SpringDoc、Knife4j 4.5.0

**支付集成**：支付宝、微信支付、PayPal、Stripe

**工具类库**：Hutool 5.8.16、Fastjson2 2.0.41、OkHttp 4.12.0

**其他**：Jakarta Mail、Kaptcha

## 项目结构

```text
sqx-server
├── sqx-application           # 应用启动模块
│   └── src/main
│       ├── java/com/sqx      # 启动类与入口
│       └── resources         # 配置文件及静态资源
├── sqx-common                # 公共组件模块
│   ├── sqx-common-bom        # 统一依赖管理
│   ├── sqx-common-core       # 核心工具与异常处理
│   ├── sqx-common-doc        # 接口文档配置
│   ├── sqx-common-file       # 文件存储（本地/OSS）
│   ├── sqx-common-mail       # 邮件服务
│   ├── sqx-common-mybatis    # MyBatis Plus 配置与插件
│   ├── sqx-common-pay        # 统一支付组件
│   ├── sqx-common-redis      # Redis 配置与工具
│   ├── sqx-common-shiro      # Shiro 安全认证封装
│   ├── sqx-common-sms        # 短信服务
│   ├── sqx-common-web        # Web 通用配置
│   └── sqx-common-websocket  # WebSocket 支持
├── sqx-modules               # 业务模块组
│   ├── sqx-app              # 移动端/App 业务接口
│   ├── sqx-generator        # 代码生成器
│   └── sqx-system           # 系统管理模块（RBAC）
└── sql                       # 数据库脚本
```

## 功能特性

- 基于 Shiro + JWT 的认证授权体系
- 完整的 RBAC 权限管理（用户、角色、菜单、按钮级权限）
- 多种文件存储支持（本地存储、阿里云 OSS）
- 集成多渠道支付（支付宝、微信、PayPal、Stripe）
- WebSocket 实时通信支持
- 邮件与短信发送功能
- 代码生成器（快速生成 CRUD 代码）
- 统一异常处理和返回结果封装
- Redis 缓存与分布式锁（Redisson）
- Knife4j 增强型 API 文档

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.8+
- MySQL 8.0+
- Redis 5.0+

### 安装步骤

1. **编译打包**
   ```bash
   mvn clean install
   ```

2. **启动应用**
   运行 `sqx-application` 模块下的 `com.sqx.SqxApplication` 主类。
   应用将在 `http://localhost:9690/sqx_fast` 启动。

### 访问 API 文档

启动成功后，访问以下地址查看 API 文档：
- **Knife4j 文档**：`http://localhost:9690/sqx_fast/doc.html`

### 默认账号

- **用户名**：admin
- **密码**：123456

## 配置说明

### 核心配置

主配置文件：`sqx-application/src/main/resources/application.yml`

```yaml
server:
  port: 9690
  servlet:
    context-path: /sqx_fast

sqx:
  jwt:
    secret: f4e2e52034348f86b67cde581c0f9eb5 # 加密秘钥
    expire: 2592000                         # Token 有效期
    header: token                           # 请求头名称
```

## 开发指南

### 项目依赖

主要依赖版本在根 `pom.xml` 中统一管理：
```xml
<properties>
    <revision>0.1</revision>
    <spring-boot.version>2.6.11</spring-boot.version>
    <mybatis-plus.version>3.5.3.2</mybatis-plus.version>
    <shiro.version>1.4.0</shiro.version>
    <redisson.version>3.18.0</redisson.version>
</properties>
```

### 添加新模块

1. 在 `sqx-modules` 下创建子模块。
2. 在父 `pom.xml` 的 `<modules>` 节点中添加声明。
3. 引入 `sqx-common-core` 核心依赖。

### 常用命令

```bash
# 清理并安装到本地仓库
mvn clean install

# 跳过测试打包
mvn clean package -DskipTests

# 运行启动模块
cd sqx-application
mvn spring-boot:run
```

## API 接口

### 认证接口
- `POST /login` - 用户登录
- `POST /logout` - 退出登录

### 系统管理
- 用户管理、角色管理、菜单管理、系统配置。
- 详细接口请参考 Knife4j 文档页面。

## 常见问题

1. **启动报错：找不到主类**
   请确认在 `sqx-application` 模块路径下执行启动命令。

2. **数据库连接失败**
   检查 `application-dev.yml` 中的驱动配置及数据库服务状态。

3. **Shiro 权限验证失败**
   确保请求头中包含正确的 `token` 字段。

## 联系方式

- **公司**：西安省钱兄网络科技有限公司
- **网站**：https://www.xiansqx.com

## 更新日志

### v0.1.0 (当前版本)
- 初始版本发布。
- 完成基础框架搭建（Spring Boot 2.6.11 + Shiro）。
- 集成 RBAC 权限管理体系。
- 支持多渠道支付与文件存储集成。
- 集成 WebSocket 实时通信与代码生成器。
