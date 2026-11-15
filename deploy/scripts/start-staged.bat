# ============================================================
# QuickBlue 分层启动脚本（Windows）
# 用法: scripts\start-staged.bat
# 前提: 镜像已构建（首次部署用 bash build.sh --all 或 Git Bash 执行）
# ============================================================
@echo off
chcp 65001 >nul
cd /d "%~dp0.."

echo [staged] 阶段 1/5：基础设施（mysql / redis / nacos）
docker compose up -d mysql redis nacos
if errorlevel 1 goto :fail

echo [staged] 阶段 2/5：Nacos 配置导入（一次性任务）
docker compose up -d nacos-init
if errorlevel 1 goto :fail
rem 等待导入完成（一次性容器，最多 180 秒）
timeout /t 30 /nobreak >nul

echo [staged] 阶段 3/5：核心服务（gateway / admin）
docker compose up -d quickblue-gateway quickblue-admin
if errorlevel 1 goto :fail
timeout /t 30 /nobreak >nul

echo [staged] 阶段 4/5：业务服务（system / business / support / ai）
docker compose up -d quickblue-system quickblue-business quickblue-support quickblue-ai
if errorlevel 1 goto :fail
timeout /t 30 /nobreak >nul

echo [staged] 阶段 5/5：前端
docker compose up -d frontend
if errorlevel 1 goto :fail

echo.
echo ================================================================
echo   全部服务已启动（各服务就绪需要 1~2 分钟，可用 docker compose ps 观察）
echo ================================================================
docker compose ps
echo.
echo   管理后台: http://localhost:8088
echo   监控中心: http://localhost:9090
goto :eof

:fail
echo [staged][失败] 启动中断，查看日志: docker compose logs <服务名>
exit /b 1
