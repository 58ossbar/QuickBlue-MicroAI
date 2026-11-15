#!/usr/bin/env bash
# ============================================================
# QuickBlue 构建部署脚本
#
# 用法:
#   bash build.sh --all              全量：构建全部镜像 + 分层启动（首次部署用）
#   bash build.sh system frontend    只构建/重启指定服务（支持短名）
#   bash build.sh --list             只打印服务清单，不做任何改动（演练）
#
# 服务短名: system business support ai gateway admin frontend
#   bash build.sh ai  等价于  bash build.sh quickblue-ai
#
# 依赖: deploy/.env（由 .env.example 复制而来）
# 宿主机只需要 Docker（含 compose 插件），JDK/Maven 均在容器内。
# ============================================================
set -e

DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"

ALL_BACKEND_SERVICES="quickblue-gateway quickblue-system quickblue-business quickblue-support quickblue-ai quickblue-admin"
ALL_SERVICES="$ALL_BACKEND_SERVICES frontend"

usage() {
    sed -n '2,/^set -e$/p' "$0" | sed '$d' | sed 's/^# \{0,1\}//'
}

# 短名归一化：ai → quickblue-ai
norm_service() {
    case "$1" in
        frontend|web|ui)                                      echo frontend ;;
        system|business|support|ai|gateway|admin)             echo "quickblue-$1" ;;
        *)                                                    echo "$1" ;;
    esac
}

# ---------- 参数解析 ----------
TARGETS=()
while [ $# -gt 0 ]; do
    case "$1" in
        --all|-a)       MODE=all ;;
        --list|-n)      MODE=list ;;
        --help|-h)      usage; exit 0 ;;
        -*)
            echo "[错误] 未知参数: $1"; usage; exit 1 ;;
        *)
            MODE=${MODE:-manual}
            TARGETS+=("$1") ;;
    esac
    shift
done
MODE=${MODE:-manual}

echo "MODE = $MODE"
[ "$MODE" = "manual" ] && echo "目标 = ${TARGETS[*]:-（空）}"

if [ "$MODE" != "list" ] && ! command -v docker >/dev/null 2>&1; then
    echo "[错误] 未找到 docker，请确认已安装 docker 与 compose 插件"
    exit 1
fi

# ---------- 确定目标服务 ----------
if [ "$MODE" = "all" ]; then
    # shellcheck disable=SC2206
    TARGETS=($ALL_SERVICES)
elif [ "$MODE" = "list" ]; then
    echo "全部服务: $ALL_SERVICES"
    exit 0
fi

if [ ${#TARGETS[@]} -eq 0 ]; then
    usage
    exit 1
fi

# 去重 + 校验
FINAL=()
for raw in "${TARGETS[@]}"; do
    s=$(norm_service "$raw")
    case " $ALL_SERVICES " in
        *" $s "*) ;;
        *) echo "[错误] 未知服务名: $raw"
           echo "        可选: $ALL_SERVICES（短名: system business support ai gateway admin frontend）"
           exit 1 ;;
    esac
    dup=0
    for x in "${FINAL[@]:-}"; do
        [ "$x" = "$s" ] && { dup=1; break; }
    done
    [ $dup -eq 0 ] && FINAL+=("$s")
done
TARGETS=("${FINAL[@]}")

echo ""
echo "    本次要构建/重启的服务: ${TARGETS[*]}"

# ---------- 构建镜像 ----------
echo ""
echo "[1/2] 构建镜像（容器内 Maven 打包，无需宿主 JDK）..."
cd "$DEPLOY_DIR"
docker compose build "${TARGETS[@]}"

# ---------- 启动 ----------
echo ""
if [ "$MODE" = "all" ]; then
    echo "[2/2] 分层启动（基础设施 → 核心 → 业务 → 前端）..."
    bash "$DEPLOY_DIR/scripts/start-staged.sh"
else
    echo "[2/2] 重建容器（只动目标服务，不碰其它服务）..."
    docker compose up -d "${TARGETS[@]}"
fi

echo ""
echo "================================================================"
echo "  部署完成: ${TARGETS[*]}"
echo "================================================================"
docker compose ps
echo ""
echo "访问: http://localhost:8088   （首次部署先完成安装向导）"
exit 0
