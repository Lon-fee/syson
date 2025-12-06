# SysON AI Services - 测试指南

## 前提条件

在开始测试之前，您需要：

1. **获取 Gemini API Key**
   - 访问: https://ai.google.dev/
   - 创建或登录 Google AI Studio账号
   - 生成 API Key

2. **设置环境变量**
   ```powershell
   # Windows PowerShell
   $env:GEMINI_API_KEY = "your-actual-api-key-here"
   
   # 或者持久化设置
   [System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'your-actual-api-key-here', 'User')
   ```

## 快速测试

### 1. 构建项目
```bash
cd c:\Users\lf\Desktop\syson-main
mvn clean install -DskipTests
```

### 2. 启动应用
```bash
cd backend\application\syson-application
mvn spring-boot:run
```

### 3. 测试API端点

#### 测试1: 健康检查
```powershell
curl http://localhost:8080/actuator/health
```

#### 测试2: AI聊天
```powershell
$body = @{
    message = "What is SysML V2?"
    context = @{}
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

#### 测试3: 代码生成
```powershell
$body = @{
    description = "Create a simple vehicle with an engine"
    context = @{}
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/code/generate" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

#### 测试4: 代码验证
```powershell
$body = @{
    code = "part def Vehicle { attribute mass : Real; }"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/code/validate" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

## 详细测试用例

### 聊天测试
```powershell
# 测试SysML专家问答
$tests = @(
    "What is a part definition?",
    "How do I create a requirement in SysML V2?",
    "Explain the difference between part def and part usage"
)

foreach ($question in $tests) {
    Write-Host "`n Testing: $question" -ForegroundColor Green
    $body = @{ message = $question; context = @{} } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
        -Method POST -ContentType "application/json" -Body $body
    Write-Host $response
}
```

### 代码生成测试
```powershell
# 测试不同复杂度的生成
$scenarios = @(
    "Create a vehicle part definition",
    "Create a vehicle with engine, transmission, and 4 wheels",
    "Create a requirement for maximum speed of 200 km/h"
)

foreach ($desc in $scenarios) {
    Write-Host "`n Generating: $desc" -ForegroundColor Green
    $body = @{ description = $desc; context = @{} } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/code/generate" `
        -Method POST -ContentType "application/json" -Body $body
    Write-Host $response.code
}
```

### 代码验证测试
```powershell
# 测试有效和无效的代码
$testCodes = @(
    "part def Vehicle { attribute mass : Real; }",  # 有效
    "part def Vehicle { attribute mass Real }",     # 缺少冒号
    "part Vehicle {}",                               # 缺少 def
    "part def vehicle { }"                           # 命名不规范
)

foreach ($code in $testCodes) {
    Write-Host "`n Validating: $code" -ForegroundColor Green
    $body = @{ code = $code } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/code/validate" `
        -Method POST -ContentType "application/json" -Body $body
    Write-Host "Valid: $($response.valid)"
    if ($response.allIssues) {
        $response.allIssues | ForEach-Object {
            Write-Host "  - [$($_.severity)] $($_.message)" -ForegroundColor Yellow
        }
    }
}
```

## 流式响应测试

流式响应需要使用特殊工具或浏览器测试：

### 使用浏览器测试
1. 打开浏览器开发者工具 (F12)
2. 在Console中运行：

```javascript
// 测试流式聊天
const eventSource = new EventSource('http://localhost:8080/api/ai/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        message: 'Explain SysML V2',
        context: {}
    })
});

eventSource.onmessage = (event) => {
    console.log('Received:', event.data);
};

eventSource.onerror = (error) => {
    console.error('Error:', error);
    eventSource.close();
};
```

## 预期结果

### ✅ 成功的聊天响应
```
SysML V2 是系统建模语言的第二个版本...
```

### ✅ 成功的代码生成
```sysml
part def Vehicle {
    attribute mass : Real;
    part engine : Engine;
}
```

### ✅ 成功的验证结果
```json
{
    "valid": true,
    "hasErrors": false,
    "hasWarnings": false,
    "errors": [],
    "warnings": [],
    "allIssues": []
}
```

## 性能基准

| 操作 | 预期响应时间 |
|------|------------|
| 简单聊天 | < 2秒 |
| 代码生成 | < 5秒 |
| 代码验证 | < 100ms |
| 流式响应首字节 | < 1秒 |

## 故障排除

### 问题1: 连接被拒绝
```
检查应用是否正在运行在8080端口
netstat -ano | findstr :8080
```

### 问题2: API Key错误
```
检查环境变量是否正确设置
echo $env:GEMINI_API_KEY
```

### 问题3: 超时错误
```
增加配置中的超时时间
syson.ai.timeout=120000
```

### 问题4: Maven构建失败
```powershell
# 清理并重新构建
mvn clean install -U -DskipTests

# 或跳过AI模块测试
cd backend\services\syson-ai-services
mvn clean install -DskipTests
```

## 下一步

测试成功后：
1. 测试前端组件集成
2. 进行端到端测试
3. 性能和负载测试
4. 部署到测试环境
