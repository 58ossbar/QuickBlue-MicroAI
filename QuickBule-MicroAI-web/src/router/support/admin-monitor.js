/*
 * 系统监控大屏 - 路由配置
 * 融合微服务入口：从菜单进入，提供原生监控大屏 + Spring Boot Admin 双视图
 */
const MonitorFrame = () => import('/@/views/support/admin/admin-monitor.vue');

export const monitorRouters = [
    {
        path: '/support/admin-monitor',
        name: 'AdminMonitor',
        meta: {
            title: '微服务监控大屏',
            needCache: false,
            needLogin: true,
        },
        component: MonitorFrame
    }
];
