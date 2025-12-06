# 🚀 快速开始 - Gemini API Key 配置

本指南将帮助您快速配置Gemini API Key并启动SysON AI服务。

## 📋 方法1: 使用自动配置脚本（推荐）

### 步骤1: 运行配置脚本
```powershell
cd c:\Users\lf\Desktop\syson-main
.\setup-env.ps1
```

脚本将引导您：
1. 输入Gemini API Key
2. 自动设置环境变量
3. 生成配置文件

### 步骤2: 手动创建 .env 文件

由于.env文件在gitignore中，需要手动创建：

**创建文件**: `c:\Users\lf\Desktop\syson-main\.env`

**内容**:
```bash
GEMINI_API_KEY=your-actual-api-key-here
```

替换`your-actual-api-key-here`为您的真实API Key。

## 📋 方法2: 直接设置环境变量

### Windows PowerShell

#### 临时设置（仅当前会话）
```powershell
$env:GEMINI_API_KEY = "your-api-key-here"
```

#### 永久设置（用户级别）
```powershell
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'your-api-key-here', 'User')
```
> 注意：需要重启PowerShell使永久设置生效

## 📋 方法3: 使用 Docker Compose（推荐生产环境）

### 步骤1: 创建 .env 文件

在项目根目录创建文件 `.env`:
```bash
GEMINI_API_KEY=your-api-key-here
```

### 步骤2: 启动服务
```bash
docker-compose up
```

Docker Compose会自动从.env文件加载环境变量！

## 🔑 获取 Gemini API Key

1. **访问** https://ai.google.dev/
2. **登录** Google账号
3. **点击** "Get API Key"
4. **选择** "Create API Key in new project" 或使用现有项目
5. **复制** 生成的API Key

## ✅ 验证配置

### 检查环境变量是否设置成功：

```powershell
# PowerShell
echo $env:GEMINI_API_KEY

# 应该显示您的API Key（前10个字符）
```

### 检查 .env 文件：

```powershell
# 显示.env文件内容（如果存在）
Get-Content .env
```

## 🏃 启动应用

### 方法1: Maven（本地开发）
```powershell
cd backend\application\syson-application
mvn spring-boot:run
```

### 方法2: Docker Compose（推荐）
```powershell
docker-compose up
```

## 🧪 测试配置

配置完成后，运行测试脚本验证：

```powershell
cd backend\services\syson-ai-services
.\test-api.ps1
```

或手动测试：
```powershell
# 等待应用启动后（约30秒）
curl http://localhost:8080/actuator/health

# 测试AI服务
$body = @{ code = "part def Vehicle {}" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/ai/code/validate" `
    -Method POST -ContentType "application/json" -Body $body
```

## ⚠️ 常见问题

### 问题1: "API Key未设置"错误
**解决方案**: 
```powershell
# 检查环境变量
echo $env:GEMINI_API_KEY

# 如果为空，重新设置
$env:GEMINI_API_KEY = "your-key"
```

### 问题2: Docker Compose找不到环境变量
**解决方案**: 确保.env文件在docker-compose.yml同目录
```powershell
# 检查.env文件位置
Test-Path .\env

# 如果不存在，创建它
@"
GEMINI_API_KEY=your-key-here
"@ | Out-File -FilePath .env -Encoding UTF8
```

### 问题3: API Key无效
**解决方案**: 
- 检查API Key是否完整（无多余空格）
- 确认API Key在Google AI Studio中已启用
- 尝试重新生成API Key

## 📊 配置文件位置总览

```
syson-main/
├── .env                          # 主环境配置（需手动创建，gitignored）
├── .env.example                  # 配置模板
├── setup-env.ps1                 # 自动配置脚本
├── docker-compose.yml            # Docker配置（已更新）
└── backend/services/syson-ai-services/
    ├── src/main/resources/
    │   └── application-ai.properties  # AI服务配置
    ├── TESTING.md                # 测试指南
    └── test-api.ps1              # 测试脚本
```

## 🎯 下一步

配置完成后：
1. ✅ 启动应用
2. ✅ 运行测试
3. ✅ 开始使用AI功能

详细测试指南请查看: [TESTING.md](backend/services/syson-ai-services/TESTING.md)
