/*
 * vite配置
 *
 */
import { resolve } from 'path';
import vue from '@vitejs/plugin-vue';
import customVariables from './src/theme/custom-variables.js';

const pathResolve = (dir) => {
    return resolve(import.meta.dirname, dir);
};
export default {
    base: process.env.NODE_ENV === 'production' ? '/admin' : '/',
    root: process.cwd(),
    resolve: {
        alias: [
            // 国际化替换
            {
                find: 'vue-i18n',
                replacement: 'vue-i18n/dist/vue-i18n.cjs.js',
            },
            // 绝对路径重命名：/@/xxxx => src/xxxx
            {
                find: /\/@\//,
                replacement: pathResolve('src') + '/',
            },
            {
                find: /^~/,
                replacement: '',
            },
        ],
    },
    server: {
        host: '0.0.0.0',
        port: 5173,
        headers: {
            // 开发模式下禁用静态资源缓存，避免修改图片后浏览器拿到 304 而看不到变化
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0',
        },
        proxy: {
            // 代理后端API
            '/api': {
                target: 'http://localhost:8080',  // Gateway地址
                changeOrigin: true, // 是否修改请求头中的 Origin 字段
                rewrite: (path) => path, // 重写路径
            },
            // 代理 actuator 健康检查
            '/actuator': {
                target: 'http://localhost:8080',  // Gateway地址
                changeOrigin: true,
                rewrite: (path) => path,
            },
        }
    },
    plugins: [vue()],
    optimizeDeps: {
        include: ['ant-design-vue/es/locale/zh_CN', 'dayjs/locale/zh-cn', 'ant-design-vue/es/locale/en_US'],
        exclude: ['vue-demi'],
    },
    build: {
        // 清除console和debugger
        terserOptions: {
            compress: {
                drop_console: true,
                drop_debugger: true,
            },
        },
        rollupOptions: {
            output: {
                //配置这个是让不同类型文件放在不同文件夹，不会显得太乱
                chunkFileNames: 'js/[name]-[hash].js',
                entryFileNames: 'js/[name]-[hash].js',
                assetFileNames: '[ext]/[name]-[hash].[ext]',
                manualChunks(id) {
                    //静态资源分拆打包
                    if (id.includes('node_modules')) {
                        return id.toString().split('node_modules/')[1].split('/')[0].toString();
                    }
                },
            },
        },
        target: 'esnext',
        outDir: 'dist', // 指定输出路径
        assetsDir: 'assets', // 指定生成静态文件目录
        assetsInlineLimit: '4096', // 小于此阈值的导入或引用资源将内联为 base64 编码
        chunkSizeWarningLimit: 500, // chunk 大小警告的限制
        minify: 'terser', // 混淆器，terser构建后文件体积更小
        emptyOutDir: true, //打包前先清空原有打包文件
    },
    css: {
        preprocessorOptions: {
            less: {
                modifyVars: customVariables,
                javascriptEnabled: true,
            },
        },
    },
    define: {
        __INTLIFY_PROD_DEVTOOLS__: false,
    },
};
