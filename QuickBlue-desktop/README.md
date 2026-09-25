# QuickBlue 桌面客户端（Tauri 2）

把 QuickBlue 前端（Vite 8 + Vue 3.5）产物打进 **Tauri 2** 壳，得到 Windows / macOS / Linux 原生桌面应用。
后端仍是原来的微服务集群（网关 8080），**后端代码零改动**。

```text
┌──────────────────────────────┐        HTTPS/HTTP         ┌─────────────────────┐
│  QuickBlue 桌面端 (Tauri 2)   │  ───────────────────────► │  QuickBlue-gateway  │
│  WebView2 + dist 静态资源     │  ◄───────────────────────  │  8080（其余微服务）  │
└──────────────────────────────┘                           └─────────────────────┘
```

## 一、它做了什么 / 没做什么

| 项 | 说明 |
| --- | --- |
| ✅ 独立窗口 | 无地址栏、无书签栏，像原生客户端 |
| ✅ 托盘常驻 | 关闭窗口隐藏到托盘，托盘菜单：显示主窗口 / 打开服务端 / 退出 |
| ✅ 单实例 | 再次双击聚焦已有窗口，不会多开 |
| ✅ 文件下载 | Excel 导出、数据库备份走**系统保存对话框**落盘（WebView 内 `<a download>` 不工作） |
| ✅ 体积小 | 前端产物 6.1 MB，安装包约 6~10 MB（不含后端，Win11 自带 WebView2） |
| ⛔ 不含后端 | Java 微服务 / MySQL / Redis / Nacos 仍在服务器上，需已部署并可访问 |
| ⛔ 后端地址不可运行时改 | 按约定**打包时写死**，改地址要重新打包 |

## 二、环境准备（Windows）

| 组件 | 版本 | 状态（本机 2026-09-22 已装） |
| --- | --- | --- |
| Node.js | ≥ 24 | ✅ v24.19 |
| Rust | stable | ✅ 1.98.1（`C:\Users\<你>\.cargo`，用户级 PATH） |
| MSVC 生成工具 | VS 2022 生成工具 17.14 | ✅ MSVC 14.44 + Windows SDK 10.0.26100 |
| WebView2 Runtime | — | Win11 自带；Win10 安装包会自动引导下载 |

新机器复现安装（本机没有 winget，用的官方安装包）：

```text
1. 下载 https://static.rust-lang.org/rustup/dist/x86_64-pc-windows-msvc/rustup-init.exe
   rustup-init.exe -y --default-toolchain stable --profile minimal
2. 下载 https://aka.ms/vs/17/release/vs_BuildTools.exe （需管理员）
   vs_BuildTools.exe --passive --wait --add Microsoft.VisualStudio.Workload.VCTools --includeRecommended
3. rustc --version && cargo --version   # 验证
```

> 安装脚本已保存在 `C:\Users\sdgs2026\quickblue-setup\`（`download.ps1` / `install-vs.ps1`），可复用。

## 三、配置后端地址（唯一必须改的地方）

编辑 `QuickBule-MicroAI-web/.env.desktop`：

```ini
VITE_APP_API_URL='http://192.168.1.10:8080/api'    # ← 改成你的网关地址
VITE_APP_TITLE='QuickBlue'
VITE_ADMIN_URL='http://localhost:9090'
```

> 地址会被 `build.rs` 同时注入 Rust 侧，托盘菜单"打开服务端地址"即用它。
> 网关已配置 `allowedOriginPatterns: "*"`，桌面端页面 origin（`http://tauri.localhost`）可直接跨域，无需改后端。

## 四、构建与运行

```powershell
cd QuickBlue-desktop
npm install                 # 首次：拉 @tauri-apps/cli
npm run dev                 # 开发：自动起 vite dev server(5173) + 桌面窗口，前端热更新
npm run build               # 打包：先构建前端产物，再产出安装包
```

产物位置（Windows）：

```text
src-tauri/target/release/bundle/nsis/QuickBlue_4.0.0_x64-setup.exe   ← 双击安装
```

> 已实测：2026-09-22 在本机构建通过，安装包 **3.14 MB**（前端产物 6.1 MB，NSIS LZMA 压缩后）。
> 构建时 `lightningcss` 会报 `:deep` / `:global` 不是合法伪类的告警，是 Vue SFC 写法与 minifier 的差异，**不影响产物**，可忽略。

- 默认只产出 **NSIS 的 `.exe` 安装器**（当前用户级安装，免管理员权限，中文界面）。
  想同时产出 `.msi`，需先装 [WiX Toolset v3](https://wixtoolset.org/)，再把 `tauri.conf.json` 的 `bundle.targets` 改为 `["nsis", "msi"]`。
- 首次打包会自动下载 NSIS 工具链（需联网），之后走缓存。
- **不支持交叉编译**：Windows 安装包只能在 Windows 机器上打（或用 GitHub Actions 的 `windows-latest` runner）。
- 代码签名（可选，消除 SmartScreen 告警）：在 `bundle.windows` 加
  ```json
  "certificateThumbprint": "<证书指纹>", "digestAlgorithm": "sha256", "timestampUrl": "http://timestamp.digicert.com"
  ```

只想验证前端产物（不用 Rust）：

```powershell
npm run build:web-only      # 等同 cd ../QuickBule-MicroAI-web && npm run build:desktop
```

## 五、目录结构

```text
QuickBlue-desktop/
├── package.json                 # 构建脚本入口
├── scripts/prepare-icons.mjs    # 从仓库现有 favicon 生成 Tauri 所需图标
└── src-tauri/
    ├── Cargo.toml               # tauri 2 + opener/dialog/fs/log/os/process/store/single-instance
    ├── build.rs                 # 读取 .env.desktop 的 VITE_APP_API_URL 注入 Rust
    ├── tauri.conf.json          # 窗口 / 托盘 / 打包配置，frontendDist 指向 web/dist-desktop
    ├── capabilities/default.json# 权限与作用域（保存路径、外链域名）
    ├── icons/                   # icon.png / icon.ico / tray.png（首次 npm run icons 自动生成）
    └── src/
        ├── main.rs              # 入口（release 隐藏控制台）
        └── lib.rs               # 托盘菜单、单实例、关闭隐藏、desktop_info 命令
```

## 六、前端侧为此做的改动（仅 3 处，对浏览器模式无影响）

| 文件 | 改动 |
| --- | --- |
| `vite.config.js` | 新增 `desktop` 模式：`base: './'`（桌面端无 `/admin` 前缀，必须相对路径） |
| `.env.desktop` | 桌面端专用环境变量，写死网关地址 |
| `src/lib/axios.js` | 下载时若检测到 `__TAURI_INTERNALS__`，改用保存对话框 + `@tauri-apps/plugin-fs` 落盘；浏览器行为完全不变 |

前端是 hash 路由（`createWebHashHistory`），天然适配 `tauri://` / `http://tauri.localhost`，无需额外改造。

## 七、已知限制

| 限制 | 说明 / 规避 |
| --- | --- |
| 后端地址写死 | 改地址需重新打包；若要运行时可配，可复用 `views/install` 向导 + `plugin-store` |
| 外链菜单（`frameFlag` 页面） | `iframe` 内嵌第三方站点会被对方 `X-Frame-Options` 挡住，建议改为 `opener` 插件用系统浏览器打开 |
| 监控中心页 | `VITE_ADMIN_URL` 指向的 Spring Boot Admin 同样受 iframe 嵌入限制 |
| 未签名安装包 | Windows SmartScreen 会告警，正式分发需代码签名证书 |
| 自动更新 | 未接入，可后续加 `tauri-plugin-updater` + 静态更新服务 |

## 八、后续可选项

- `tauri-plugin-updater`：静默更新
- `tauri-plugin-autostart`：开机自启
- 路线 B（sidecar 内置后端）：`jlink` 精简 JRE + 各服务 jar 打进 resources，桌面端启动/退出托管 Java 进程，适合本地 Ollama + 私有知识库场景
