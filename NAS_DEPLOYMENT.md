# 🚀 SysON AI - 飞牛NAS 部署指南

本指南详细介绍如何将 SysON AI 系统部署到飞牛NAS (FnOS)。

---

## 📋 前置要求

### NAS 硬件要求
- **CPU**: x86_64 架构 (Intel/AMD)
- **内存**: 建议 4GB+ (最低 2GB)
- **存储**: 至少 10GB 可用空间

### 软件要求
- **飞牛NAS系统** (FnOS) 最新版本
- **Docker** 已安装并启用
- **SSH** 访问权限 (可选但推荐)

---

## 🔧 方法一：使用预构建镜像（推荐）

### 步骤 1: 在电脑上构建镜像

在您的 Windows 电脑上执行：

```powershell
# 进入项目目录
cd c:\Users\lf\Desktop\syson-main

# 构建Docker镜像
docker build -t syson-ai:latest .

# 导出镜像为tar文件
docker save syson-ai:latest -o syson-ai.tar
```

### 步骤 2: 传输镜像到NAS

使用 SCP 或 Samba 共享将 `syson-ai.tar` 传输到NAS：

```powershell
# 使用SCP (替换NAS_IP为您的NAS地址)
scp syson-ai.tar admin@NAS_IP:/volume1/docker/
```

或者通过飞牛NAS文件管理器上传。

### 步骤 3: 在NAS上加载镜像

SSH 登录到 NAS：

```bash
# 登录NAS
ssh admin@NAS_IP

# 加载镜像
docker load -i /volume1/docker/syson-ai.tar

# 验证镜像
docker images | grep syson-ai
```

### 步骤 4: 部署容器

1. 将 `docker-compose.nas.yml` 和 `.env` 上传到 NAS
2. SSH 登录 NAS 执行：

```bash
# 进入配置目录
cd /volume1/docker/syson

# 启动服务
docker-compose -f docker-compose.nas.yml up -d

# 查看日志
docker-compose -f docker-compose.nas.yml logs -f
```

---

## 🔧 方法二：使用飞牛NAS Docker管理界面

### 步骤 1: 上传镜像

1. 登录飞牛NAS Web管理界面
2. 进入 **Docker** → **镜像**
3. 点击 **导入** → 选择 `syson-ai.tar`

### 步骤 2: 创建数据库容器

1. 进入 **Docker** → **容器** → **创建**
2. 配置如下：
   - **镜像**: `postgres:15`
   - **容器名称**: `syson-database`
   - **环境变量**:
     - `POSTGRES_DB=syson`
     - `POSTGRES_USER=syson`
     - `POSTGRES_PASSWORD=your_password`
   - **卷映射**: `/your/path/db:/var/lib/postgresql/data`
   - **网络**: 创建新网络 `syson-network`

### 步骤 3: 创建应用容器

1. **Docker** → **容器** → **创建**
2. 配置如下：
   - **镜像**: `syson-ai:latest`
   - **容器名称**: `syson-app`
   - **端口映射**: `8080:8080`
   - **环境变量**:
     - `SPRING_DATASOURCE_URL=jdbc:postgresql://syson-database:5432/syson`
     - `SPRING_DATASOURCE_USERNAME=syson`
     - `SPRING_DATASOURCE_PASSWORD=your_password`
     - `GEMINI_API_KEY=你的API密钥`
     - `SPRING_PROFILES_ACTIVE=ai`
   - **网络**: 选择 `syson-network`

### 步骤 4: 启动容器

1. 先启动 `syson-database`
2. 等待数据库就绪（约30秒）
3. 再启动 `syson-app`

---

## ⚙️ 配置文件说明

### .env 文件内容

```bash
# 必须配置
GEMINI_API_KEY=你的Gemini_API密钥

# 可选配置
POSTGRES_PASSWORD=syson_secure_password
```

### docker-compose.nas.yml 重要配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| GEMINI_API_KEY | Gemini AI API密钥 | (必填) |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | syson_password_change_me |
| JAVA_OPTS | JVM内存设置 | -Xms512m -Xmx2g |
| 端口 | Web服务端口 | 8080 |

---

## 🌐 访问应用

部署完成后，通过浏览器访问：

```
http://NAS_IP:8080
```

### 测试 AI 服务

```bash
# 在NAS上测试（或从任何能访问NAS的设备）
curl -X POST http://NAS_IP:8080/api/ai/code/validate \
  -H "Content-Type: application/json" \
  -d '{"code": "part def Vehicle { attribute mass : Real; }"}'
```

---

## 📊 资源监控

### 查看容器状态

```bash
# SSH登录NAS后
docker ps

# 查看资源使用
docker stats syson-app syson-database
```

### 查看日志

```bash
# 应用日志
docker logs syson-app -f

# 数据库日志
docker logs syson-database -f
```

---

## 🔄 更新部署

### 更新应用

```bash
# 停止容器
docker-compose -f docker-compose.nas.yml down

# 删除旧镜像
docker rmi syson-ai:latest

# 加载新镜像
docker load -i syson-ai-new.tar

# 重新启动
docker-compose -f docker-compose.nas.yml up -d
```

---

## 🛠️ 故障排除

### 问题：容器启动失败

```bash
# 查看详细日志
docker logs syson-app

# 常见原因：
# 1. 数据库未就绪 - 等待30秒后重试
# 2. 内存不足 - 调整JAVA_OPTS
# 3. 端口冲突 - 修改端口映射
```

### 问题：无法连接数据库

```bash
# 检查数据库容器状态
docker logs syson-database

# 验证网络连通性
docker exec syson-app ping syson-database
```

### 问题：AI服务返回错误

```bash
# 检查API Key是否正确设置
docker exec syson-app env | grep GEMINI

# 测试API Key有效性（在任意终端）
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent" \
  -H "Content-Type: application/json" \
  -H "X-goog-api-key: YOUR_API_KEY" \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

---

## 📁 文件清单

部署所需文件：

```
传输到NAS的文件/
├── syson-ai.tar              # Docker镜像 (从电脑构建)
├── docker-compose.nas.yml    # NAS专用配置
└── .env                      # 环境变量 (包含API Key)
```

---

## 🎯 快速命令参考

```bash
# 构建镜像 (在电脑上)
docker build -t syson-ai:latest .

# 导出镜像
docker save syson-ai:latest -o syson-ai.tar

# 加载镜像 (在NAS上)
docker load -i syson-ai.tar

# 启动服务
docker-compose -f docker-compose.nas.yml up -d

# 停止服务
docker-compose -f docker-compose.nas.yml down

# 查看日志
docker-compose -f docker-compose.nas.yml logs -f

# 重启服务
docker-compose -f docker-compose.nas.yml restart
```

---

祝部署顺利！🎉
