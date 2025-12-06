@echo off
REM SysON AI - Docker 构建脚本
REM 用于构建并导出Docker镜像

echo ============================================
echo   SysON AI - Docker 构建脚本
echo ============================================
echo.

REM 检查Docker是否可用
docker version >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker 未找到或未启动
    echo 请确保 Docker Desktop 已启动
    pause
    exit /b 1
)

echo [✓] Docker 已就绪
echo.

REM 构建镜像
echo [1/3] 正在构建 Docker 镜像 (可能需要5-10分钟)...
echo.
docker build -t syson-ai:latest .
if errorlevel 1 (
    echo.
    echo [错误] Docker 构建失败
    pause
    exit /b 1
)

echo.
echo [✓] 镜像构建成功
echo.

REM 导出镜像
echo [2/3] 正在导出镜像为 syson-ai.tar...
docker save syson-ai:latest -o syson-ai.tar
if errorlevel 1 (
    echo.
    echo [错误] 镜像导出失败
    pause
    exit /b 1
)

echo.
echo [✓] 镜像导出成功
echo.

REM 完成
echo [3/3] 构建完成!
echo.
echo ============================================
echo   输出文件:
echo   - syson-ai.tar (Docker镜像)
echo.
echo   下一步:
echo   1. 将 syson-ai.tar 传输到飞牛NAS
echo   2. 在NAS上导入: docker load -i syson-ai.tar
echo   3. 使用 docker-compose.nas.yml 启动服务
echo ============================================
echo.

pause
