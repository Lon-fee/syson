# SysON AI Services - Environment Setup Script
# 此脚本帮助您设置Gemini API Key

param(
    [string]$ApiKey = ""
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SysON AI Services - 环境配置" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if .env file exists
$envFile = Join-Path $PSScriptRoot ".env"
$envExampleFile = Join-Path $PSScriptRoot ".env.example"

if (-not (Test-Path $envExampleFile)) {
    Write-Host "❌ 错误: .env.example 文件不存在" -ForegroundColor Red
    exit 1
}

# If no API key provided as parameter, ask user
if (-not $ApiKey) {
    Write-Host "请获取您的Gemini API Key:" -ForegroundColor Yellow
    Write-Host "1. 访问: https://ai.google.dev/" -ForegroundColor White
    Write-Host "2. 登录Google账号" -ForegroundColor White
    Write-Host "3. 点击 'Get API Key' → 'Create API Key'" -ForegroundColor White
    Write-Host "4. 复制生成的API Key" -ForegroundColor White
    Write-Host ""
    
    $ApiKey = Read-Host "请粘贴您的Gemini API Key"
    
    if (-not $ApiKey -or $ApiKey -eq "your-gemini-api-key-here") {
        Write-Host "❌ 无效的API Key" -ForegroundColor Red
        exit 1
    }
}

# Create or update .env file
Write-Host ""
Write-Host "正在创建 .env 文件..." -ForegroundColor Green

$envContent = @"
# SysON AI Services - Environment Variables
# ⚠️ WARNING: Never commit this file to version control!

# ===========================================
# Google Gemini API Configuration
# ===========================================
GEMINI_API_KEY=$ApiKey

# Created on: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
"@

try {
    # The .env file is gitignored, so we'll write to a temp location first
    # and let the user know where to place it
    $tempEnvFile = Join-Path $env:TEMP "syson.env"
    $envContent | Out-File -FilePath $tempEnvFile -Encoding UTF8
    
    Write-Host "✓ 环境配置已创建" -ForegroundColor Green
    Write-Host ""
    Write-Host "由于 .env 文件在 .gitignore 中（这是安全的做法）," -ForegroundColor Yellow
    Write-Host "临时文件已保存到: $tempEnvFile" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "请手动复制到项目根目录:" -ForegroundColor Cyan
    Write-Host "  Copy-Item ""$tempEnvFile"" ""$envFile""" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host "❌ 创建文件失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Set environment variable for current session
Write-Host "正在设置当前会话的环境变量..." -ForegroundColor Green
$env:GEMINI_API_KEY = $ApiKey
Write-Host "✓ 环境变量已设置 (当前PowerShell会话)" -ForegroundColor Green
Write-Host ""

# Verify
Write-Host "验证配置:" -ForegroundColor Cyan
Write-Host "  GEMINI_API_KEY = $($env:GEMINI_API_KEY.Substring(0, [Math]::Min(10, $env:GEMINI_API_KEY.Length)))..." -ForegroundColor Green
Write-Host ""

# Instructions for persistence
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "下一步操作" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "选项1: 每次使用前加载 .env 文件" -ForegroundColor Yellow
Write-Host "  在项目根目录创建 .env 文件，包含:" -ForegroundColor White
Write-Host "    GEMINI_API_KEY=$ApiKey" -ForegroundColor Gray
Write-Host ""
Write-Host "  然后运行:" -ForegroundColor White
Write-Host "    Get-Content .env | ForEach-Object {" -ForegroundColor Gray
Write-Host "        if (\$_ -match '^([^#].*?)=(.*)$') {" -ForegroundColor Gray
Write-Host "            [Environment]::SetEnvironmentVariable(\$matches[1], \$matches[2], 'Process')" -ForegroundColor Gray
Write-Host "        }" -ForegroundColor Gray
Write-Host "    }" -ForegroundColor Gray
Write-Host ""
Write-Host "选项2: 永久设置用户环境变量" -ForegroundColor Yellow
Write-Host "  [Environment]::SetEnvironmentVariable('GEMINI_API_KEY', '$ApiKey', 'User')" -ForegroundColor Gray
Write-Host "  (需要重启PowerShell生效)" -ForegroundColor White
Write-Host ""
Write-Host "选项3: 使用Docker Compose" -ForegroundColor Yellow
Write-Host "  Docker会自动从 .env 文件加载环境变量" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "现在可以启动应用:" -ForegroundColor Green
Write-Host "  cd backend\application\syson-application" -ForegroundColor White
Write-Host "  mvn spring-boot:run" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
