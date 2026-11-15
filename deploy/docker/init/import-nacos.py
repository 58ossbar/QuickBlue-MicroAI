#!/usr/bin/env python3
# ============================================================
# QuickBlue Nacos 配置导入脚本（适配 Nacos 2.x，与后端 client 2.4.3 同版本）
#
# 由 nacos-init 一次性容器执行：
#   1. 等待 Nacos 就绪
#   2. 创建命名空间（默认 QuickBlue-dev）
#   3. 导入 common/（共享配置）与 services/（各服务配置）下全部 yaml
#
# API 说明（Nacos 2.x）:
#   - 登录:        POST /nacos/v1/auth/login        （鉴权开启时）
#   - 命名空间:    GET/POST /nacos/v1/console/namespaces
#   - 发布配置:    POST /nacos/v1/cs/configs
# ============================================================
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

NACOS_ADDR = os.environ.get("NACOS_ADDR", "nacos:8848")
BASE = f"http://{NACOS_ADDR}/nacos"
NAMESPACE = os.environ.get("NACOS_NAMESPACE", "QuickBlue-dev")
GROUP = os.environ.get("NACOS_GROUP", "QuickBlue_GROUP")
USERNAME = os.environ.get("NACOS_USERNAME", "")
PASSWORD = os.environ.get("NACOS_PASSWORD", "")

# 配置目录：compose 挂载到 /config（common/ + services/）
_env_dir = os.environ.get("CONFIG_DIR")
CONFIG_DIR = Path(_env_dir) if _env_dir else (Path("/config") if Path("/config").exists() else Path("./nacos_config"))

_access_token = None


def http_post(path: str, params: dict, timeout: int = 15) -> str:
    data = urllib.parse.urlencode(params).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=data, method="POST")
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read().decode("utf-8")


def http_get(path: str, params: dict, timeout: int = 15) -> str:
    query = urllib.parse.urlencode(params)
    req = urllib.request.Request(f"{BASE}{path}?{query}", method="GET")
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read().decode("utf-8")


def login():
    """鉴权开启时登录换取 accessToken；未配置账号/失败时保持匿名（内网部署预期行为）。"""
    global _access_token
    if not USERNAME:
        print("[nacos-init] 未配置 NACOS_USERNAME，跳过登录（匿名访问，对应关闭鉴权部署）")
        return
    try:
        body = http_post("/v1/auth/login", {"username": USERNAME, "password": PASSWORD})
        _access_token = json.loads(body).get("accessToken")
        print("[nacos-init] 登录成功")
    except Exception as e:
        print(f"[nacos-init] 登录失败（鉴权关闭时可忽略）: {e}")


def auth() -> dict:
    if _access_token:
        return {"accessToken": _access_token}
    if USERNAME:
        return {"username": USERNAME, "password": PASSWORD}
    return {}


def wait_ready(minutes: int = 5):
    deadline = time.time() + minutes * 60
    while time.time() < deadline:
        try:
            http_get("/", {}, timeout=5)
            print("[nacos-init] Nacos 已就绪")
            return
        except Exception:
            time.sleep(5)
    print("[nacos-init][失败] 等待 Nacos 就绪超时", file=sys.stderr)
    sys.exit(1)


def ensure_namespace():
    try:
        body = http_get("/v1/console/namespaces", {"pageNum": 1, "pageSize": 100, **auth()})
        items = json.loads(body).get("data") or []
        if isinstance(items, dict):
            items = items.get("content") or []
        for ns in items:
            if ns.get("namespace") == NAMESPACE:
                print(f"[nacos-init] 命名空间已存在: {NAMESPACE}")
                return
    except Exception as e:
        print(f"[nacos-init] 查询命名空间失败（继续尝试创建）: {e}")
    try:
        http_post("/v1/console/namespaces", {
            "customNamespaceId": NAMESPACE,
            "namespaceName": NAMESPACE,
            "namespaceDesc": "QuickBlue 环境",
            **auth(),
        })
        print(f"[nacos-init] 命名空间已创建: {NAMESPACE}")
    except Exception as e:
        print(f"[nacos-init][失败] 创建命名空间 {NAMESPACE}: {e}", file=sys.stderr)
        sys.exit(1)


def publish(data_id: str, content: str) -> bool:
    try:
        result = http_post("/v1/cs/configs", {
            "tenant": NAMESPACE,
            "dataId": data_id,
            "group": GROUP,
            "content": content,
            "type": "yaml",
            **auth(),
        })
        return result.strip().lower() == "true"
    except Exception as e:
        print(f"    发布失败 {data_id}: {e}")
        return False


def import_dir(sub: str):
    d = CONFIG_DIR / sub
    if not d.is_dir():
        print(f"[nacos-init] 目录不存在，跳过: {d}")
        return 0, 0
    ok = fail = 0
    for f in sorted(d.glob("*.yaml")) + sorted(d.glob("*.yml")):
        content = f.read_text(encoding="utf-8-sig").replace("\r\n", "\n")
        if publish(f.name, content):
            print(f"    [发布] {sub}/{f.name}")
            ok += 1
        else:
            print(f"    [失败] {sub}/{f.name}")
            fail += 1
    return ok, fail


def main():
    print(f"[nacos-init] 目标: {NACOS_ADDR} 命名空间: {NAMESPACE} 分组: {GROUP}")
    wait_ready()
    login()
    ensure_namespace()
    ok, fail = import_dir("common")
    ok2, fail2 = import_dir("services")
    print(f"[nacos-init] 导入完成: 成功 {ok + ok2} 个, 失败 {fail + fail2} 个")
    if fail + fail2 > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
