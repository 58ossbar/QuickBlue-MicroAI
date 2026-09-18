/*
 * 所有路由入口
 *
 */
import { homeRouters } from './system/home';
import { loginRouters } from './system/login';
import { helpDocRouters } from './support/help-doc';
import { changeLogRouters } from './support/change-log';
import { monitorRouters } from './support/admin-monitor';
import { installRouter } from './install';
import NotFound from '/@/views/system/40X/404.vue';
import NoPrivilege from '/@/views/system/40X/403.vue';

export const routerArray = [
    // 首页重定向到登录页面
    { path: '/', name: 'home', redirect: '/login' },
    // 安装向导路由（放在前面，确保优先匹配）
    installRouter,
    // 安装向导路由 - HTML扩展名访问
    { path: '/install.html', name: 'install-html', redirect: '/install' },
    ...loginRouters,
    ...homeRouters,
    ...helpDocRouters,
    ...changeLogRouters,
    ...monitorRouters,
    { path: '/:pathMatch(.*)*', name: '404', component: NotFound },
    { path: '/403', name: '403', component: NoPrivilege }
];
