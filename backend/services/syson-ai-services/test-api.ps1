# SysON AI Services - 自动化测试脚本
# 使用PowerShell运行此脚本

param(
    [string]$ApiKey = $env:GEMINI_API_KEY,
    [string]$BaseUrl = "http://localhost:8080/api/ai"
)

Write-Host "================================" -ForegroundColor Cyan
Write-Host "SysON AI Services 测试脚本" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# 检查API Key
if ([string]::IsNullOrEmpty($ApiKey)) {
    Write-Host "❌ 错误: GEMINI_API_KEY 未设置" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先设置API Key:" -ForegroundColor Yellow
    Write-Host '  $env:GEMINI_API_KEY = "your-api-key-here"' -ForegroundColor Yellow
    Write-Host ""
    Write-Host "或使用参数运行:" -ForegroundColor Yellow
    Write-Host '  .\test-api.ps1 -ApiKey "your-api-key-here"' -ForegroundColor Yellow
    exit 1
}

# 设置环境变量供后续使用
$env:GEMINI_API_KEY = $ApiKey

Write-Host "✓ API Key已设置" -ForegroundColor Green
Write-Host "✓ 测试地址: $BaseUrl" -ForegroundColor Green
Write-Host ""

# 测试计数器
$passed = 0
$failed = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [hashtable]$Body
    )
    
    Write-Host "测试: $Name" -ForegroundColor Cyan
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $jsonBody = $Body | ConvertTo-Json -Depth 10
        Write-Host "  请求: $jsonBody" -ForegroundColor Gray
        
        $response = Invoke-RestMethod -Uri $Url `
            -Method POST `
            -ContentType "application/json" `
            -Body $jsonBody `
            -TimeoutSec 30
        
        Write-Host "  ✓ 成功" -ForegroundColor Green
        # 简化输出，避免过长
        $jsonResponse = $response | ConvertTo-Json -Depth 3 -Compress
        if ($jsonResponse.Length -gt 500) {
            $jsonResponse = $jsonResponse.Substring(0, 500) + "..."
        }
        Write-Host "  响应: $jsonResponse" -ForegroundColor Gray
        Write-Host ""
        
        $script:passed++
        return $response
    }
    catch {
        Write-Host "  ✗ 失败" -ForegroundColor Red
        Write-Host "  错误: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
             $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
             $errBody = $reader.ReadToEnd()
             Write-Host "  详情: $errBody" -ForegroundColor Red
        }
        Write-Host ""
        
        $script:failed++
        return $null
    }
}

# =====================================
# 测试1: 代码验证 (不需要AI)
# =====================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "测试组1: 代码验证 (本地)" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

# 测试有效代码
Test-Endpoint -Name "验证有效代码" `
    -Url "$BaseUrl/code/validate" `
    -Body @{
        code = "part def Vehicle { attribute mass : Real; }"
    }

# 测试无效代码
$result = Test-Endpoint -Name "验证无效代码(应检测到错误)" `
    -Url "$BaseUrl/code/validate" `
    -Body @{
        code = "part def Vehicle { attribute mass Real }"  # 缺少冒号
    }

if ($result -and -not $result.valid) {
    Write-Host "  ✓ 正确检测到验证错误" -ForegroundColor Green
    Write-Host ""
}

# =====================================
# 测试2: AI聊天
# =====================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "测试组2: AI聊天" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

Test-Endpoint -Name "简单问答" `
    -Url "$BaseUrl/chat" `
    -Body @{
        message = "What is SysML V2?"
        context = @{}
    }

Test-Endpoint -Name "技术问题" `
    -Url "$BaseUrl/chat" `
    -Body @{
        message = "How do I create a part definition?"
        context = @{
            elementType = "PartDefinition"
        }
    }

# =====================================
# 测试3: 代码生成
# =====================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "测试组3: 代码生成" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$generatedCode = Test-Endpoint -Name "生成简单模型" `
    -Url "$BaseUrl/code/generate" `
    -Body @{
        description = "Create a vehicle with an engine"
        context = @{}
    }

if ($generatedCode -and $generatedCode.code) {
    Write-Host "生成的代码:" -ForegroundColor Cyan
    Write-Host $generatedCode.code -ForegroundColor White
    Write-Host ""
    
    # 验证生成的代码
    Write-Host "验证生成的代码..." -ForegroundColor Cyan
    $validation = Test-Endpoint -Name "验证生成的代码" `
        -Url "$BaseUrl/code/validate" `
        -Body @{
            code = $generatedCode.code
        }
    
    if ($validation -and $validation.valid) {
        Write-Host "  ✓ 生成的代码有效!" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 生成的代码存在问题" -ForegroundColor Yellow
        if ($validation.allIssues) {
            $validation.allIssues | ForEach-Object {
                Write-Host "    - [$($_.severity)] $($_.message)" -ForegroundColor Yellow
            }
        }
    }
    Write-Host ""
}

# =====================================
# 测试4: 模型分析
# =====================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "测试组4: 模型分析" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

Test-Endpoint -Name "分析部件定义" `
    -Url "$BaseUrl/analyze" `
    -Body @{
        content = "part def Vehicle { attribute mass : Real; }"
        elementType = "PartDefinition"
    }

# =====================================
# 测试5: 获取建议
# =====================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "测试组5: 建模建议" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

Test-Endpoint -Name "获取建模建议" `
    -Url "$BaseUrl/suggestions" `
    -Body @{
        context = @{
            elementType = "PartDefinition"
            elementName = "Vehicle"
        }
    }

# =====================================
# 测试总结
# =====================================
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "测试总结" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "通过: $passed" -ForegroundColor Green
Write-Host "失败: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($failed -eq 0) {
    Write-Host "🎉 所有测试通过!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "⚠ 部分测试失败，请检查日志" -ForegroundColor Yellow
    exit 1
}
