use tauri::menu::{MenuBuilder, MenuItemBuilder};
use tauri::{Manager, WindowEvent};
use tauri_plugin_opener::OpenerExt;

const WINDOW_LABEL: &str = "main";
const TRAY_ID: &str = "main";

/// 打包时由 build.rs 从 .env.desktop 注入的网关地址
const API_URL: &str = env!("QUICKBLUE_API_URL");

/// 暴露给前端的桌面端信息，便于"关于/诊断"页展示
#[tauri::command]
fn desktop_info() -> serde_json::Value {
    serde_json::json!({
        "platform": std::env::consts::OS,
        "arch": std::env::consts::ARCH,
        "version": env!("CARGO_PKG_VERSION"),
        "apiBaseUrl": API_URL,
    })
}

/// 唤起（并聚焦）主窗口
fn focus_main_window(app: &tauri::AppHandle) {
    if let Some(window) = app.get_webview_window(WINDOW_LABEL) {
        let _ = window.unminimize();
        let _ = window.show();
        let _ = window.set_focus();
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        // 只允许单实例：再次双击时聚焦已运行的窗口，而不是再开一个
        .plugin(tauri_plugin_single_instance::init(|app, _args, _cwd| {
            focus_main_window(app);
        }))
        .plugin(tauri_plugin_log::Builder::new().build())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![desktop_info])
        .setup(|app| {
            let handle = app.handle().clone();

            // ---------------- 托盘菜单 ----------------
            let show_item = MenuItemBuilder::with_id("show", "显示主窗口").build(&handle)?;
            let api_item = MenuItemBuilder::with_id("api", "打开服务端地址").build(&handle)?;
            let quit_item = MenuItemBuilder::with_id("quit", "退出").build(&handle)?;

            let menu = MenuBuilder::new(&handle)
                .item(&show_item)
                .item(&api_item)
                .separator()
                .item(&quit_item)
                .build()?;

            if let Some(tray) = app.tray_by_id(TRAY_ID) {
                let _ = tray.set_menu(Some(menu));
            }

            handle.on_menu_event(move |app, event| match event.id().as_ref() {
                "show" => focus_main_window(app),
                "api" => {
                    // 网关根路径（去掉 /api 后缀）用系统默认浏览器打开
                    let url = API_URL.trim_end_matches('/').trim_end_matches("/api");
                    if !url.is_empty() {
                        let _ = app.opener().open_url(url, None::<&str>);
                    }
                }
                "quit" => app.exit(0),
                _ => {}
            });

            Ok(())
        })
        // 点右上角关闭：隐藏到托盘，不退出进程
        .on_window_event(|window, event| {
            if let WindowEvent::CloseRequested { api, .. } = event {
                let _ = window.hide();
                api.prevent_close();
            }
        })
        .run(tauri::generate_context!())
        .expect("启动 QuickBlue 桌面端失败");
}
