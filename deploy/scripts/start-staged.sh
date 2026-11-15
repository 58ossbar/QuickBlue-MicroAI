#!/usr/bin/env bash
# ============================================================
# QuickBlue 分层启动脚本（Linux/Mac）
#
# 用途：按依赖顺序错峰启动全部服务，避免多个 JVM 同时初始化把
#       CPU/内存打满；每阶段等待上一阶段健康后再继续。
#
# 用法:
#   bash scripts/start-staged.sh
#   bash scripts/start-staged.sh --profile ollama   # 连同本地嵌入模型一起启动
#
# 前提：镜像已构建（首次部署用 bash build.sh --all）
# ============================================================
set -e

DEPLOY_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$DEPLOY_DIR"

PROFILE_ARGS=()
if [ "$1" = "--profile" ] && [ -n "$2" ]; then
    PROFILE_ARGS=(--profile "$2")
fi

STAGE_INTERVAL="${STAGE_INTERVAL:-10}"   # 阶段间隔秒数

log()   { echo "[staged] $*"; }
fail()  { echo "[staged][失败] $*" >&2; exit 1; }

# 等待指定服务达到 healthy；task=exit 时等待一次性任务成功退出
wait_service() {
    local svc=$1 task=$2 timeout=${3:-300}
    log "等待 $svc ..."
    local waited=0
    while [ "$waited" -lt "$timeout" ]; do
        if [ "$task" = "exit" ]; then
            state=$(docker compose ps "$svc" --format json 2>/dev/null | grep -o '"ExitCode":[0-9]*' | grep -o '[0-9]*' || true)
            if [ -n "$state" ]; then
                [ "$state" = "0" ] && return 0 || fail "$svc 非正常退出（ExitCode=$state），查看日志: docker compose logs $svc"
            fi
        else
            if docker compose ps "$svc" 2>/dev/null | grep -q "healthy"; then
                return 0
            fi
        fi
        sleep 5
        waited=$((waited + 5))
        [ $((waited % 60)) -eq 0 ] && log "  已等待 ${waited}s ..."
    done
    fail "$svc 等待超时（${timeout}s），查看日志: docker compose logs $svc"
}

log "阶段 1/5：基础设施（mysql / redis / nacos）"
docker compose "${PROFILE_ARGS[@]}" up -d mysql redis nacos
wait_service mysql
wait_service redis
wait_service nacos
sleep "$STAGE_INTERVAL"

log "阶段 2/5：Nacos 配置导入（一次性任务）"
docker compose "${PROFILE_ARGS[@]}" up -d nacos-init
wait_service nacos-init exit 180

log "阶段 3/5：核心服务（gateway / admin）"
docker compose "${PROFILE_ARGS[@]}" up -d quickblue-gateway quickblue-admin
wait_service quickblue-gateway
sleep "$STAGE_INTERVAL"

log "阶段 4/5：业务服务（system / business / support / ai）"
docker compose "${PROFILE_ARGS[@]}" up -d quickblue-system quickblue-business quickblue-support quickblue-ai
wait_service quickblue-system
wait_service quickblue-business
wait_service quickblue-support
wait_service quickblue-ai
sleep "$STAGE_INTERVAL"

log "阶段 5/5：前端"
docker compose "${PROFILE_ARGS[@]}" up -d frontend

echo ""
echo "================================================================"
echo "  全部服务已启动"
echo "================================================================"
docker compose ps
echo ""
echo "  管理后台: http://localhost:8088"
echo "  监控中心: http://localhost:9090"
echo "  API 文档: http://localhost:8088/docs/"
echo ""
echo "  首次部署请先完成安装向导（前端页面会自动引导）。"
