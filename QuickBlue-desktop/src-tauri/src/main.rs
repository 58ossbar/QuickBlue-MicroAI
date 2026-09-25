// 隐藏 Windows 上 release 模式的控制台窗口
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

fn main() {
    quickblue_desktop_lib::run()
}
