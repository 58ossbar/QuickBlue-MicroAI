#!/bin/bash
# ============================================================
# QuickBlue MySQL 初始化（首次启动自动执行）
#
# 执行时机：mysql 容器【首次启动】（数据卷为空）时，由
#           /docker-entrypoint-initdb.d/ 机制自动调用。
#           之后重启/升级不会再执行（数据卷已有数据）。
#
# 建库授权：01 脚本自含（CREATE DATABASE IF NOT EXISTS + 专用账号）
# 导库顺序：02 support -> 03 system -> 04 business -> 05 ai
#           （SQL 文件不含 USE 语句，按库分别导入）
#
# 手动重导（清空数据卷后重建容器即可自动重跑）：
#   docker compose down -v && docker compose up -d mysql
# ============================================================
set -e

SQL_DIR=${SQL_DIR:-/sql}
log() { echo "[init-mysql] $*"; }

for f in "$SQL_DIR"/*.sql; do
    # Windows 编辑器可能把脚本存成 CRLF，导入前统一去掉 \r
    tr -d '\r' < "$f" > "/tmp/$(basename "$f")"
done

log "1/5 建库 + 专用账号（01_create_databases.sql）"
mysql --default-character-set=utf8mb4 < /tmp/01_create_databases.sql

log "2/5 导入 support 库（02_migrate_support.sql）"
mysql --default-character-set=utf8mb4 quickblue_support < /tmp/02_migrate_support.sql

log "3/5 导入 system 库（03_migrate_system.sql）"
mysql --default-character-set=utf8mb4 quickblue_system < /tmp/03_migrate_system.sql

log "4/5 导入 business 库（04_migrate_business.sql）"
mysql --default-character-set=utf8mb4 quickblue_business < /tmp/04_migrate_business.sql

log "5/5 导入 ai 库（05_create_ai_tables.sql）"
mysql --default-character-set=utf8mb4 quickblue_ai < /tmp/05_create_ai_tables.sql

log "初始化完成：4 个业务库 + 全部表结构 + 种子数据"
