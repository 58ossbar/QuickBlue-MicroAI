/*
 * 准备 Tauri 构建所需图标
 *
 * 若 src-tauri/icons/ 下已有同名文件则跳过（方便替换成自己的 logo）。
 * 首次执行会从仓库内已有资源复制一份默认图标。
 */
import { copyFileSync, existsSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const iconsDir = resolve(root, 'src-tauri/icons');

// 路径相对本工程根目录 QuickBlue-desktop/（与 src-tauri 下的配置相差一级）
const jobs = [
  ['../QuickBlue-MicroAI/doc/images/favicon.png', 'icon.png'],
  ['../QuickBlue-MicroAI/doc/images/favicon.png', 'tray.png'],
  ['../QuickBule-MicroAI-web/public/favicon.ico', 'icon.ico'],
];

mkdirSync(iconsDir, { recursive: true });

let copied = 0;
for (const [from, to] of jobs) {
  const src = resolve(root, from);
  const dest = resolve(iconsDir, to);
  if (existsSync(dest)) {
    console.log(`[icons] 已存在，跳过：${to}`);
    continue;
  }
  if (!existsSync(src)) {
    console.warn(`[icons] 源文件缺失，跳过：${from}`);
    continue;
  }
  copyFileSync(src, dest);
  copied += 1;
  console.log(`[icons] 已生成：${to}`);
}

console.log(`[icons] 完成，复制 ${copied} 个文件。自定义 logo 请替换 src-tauri/icons/ 下同名文件（建议 icon.png ≥512x512）。`);
