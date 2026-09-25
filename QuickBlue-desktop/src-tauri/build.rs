use std::fs;
use std::path::PathBuf;

fn main() {
    // 桌面端后端地址由前端 .env.desktop 决定，这里同步注入到 Rust 侧，
    // 供托盘菜单"打开服务端地址"使用；文件缺失时退回空串（菜单项自动隐藏）。
    let env_file = PathBuf::from("../../QuickBule-MicroAI-web/.env.desktop");
    let api_url = fs::read_to_string(&env_file)
        .unwrap_or_default()
        .lines()
        .find_map(|line| line.trim().strip_prefix("VITE_APP_API_URL="))
        .map(|v| v.trim().trim_matches(|c| c == '\'' || c == '"').to_string())
        .unwrap_or_default();

    println!("cargo:rustc-env=QUICKBLUE_API_URL={}", api_url);
    println!("cargo:rerun-if-changed={}", env_file.display());

    tauri_build::build()
}
