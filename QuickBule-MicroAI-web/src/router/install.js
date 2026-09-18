/*
 * 安装向导路由
 */

export const installRouter = {
  path: '/install',
  name: 'install',
  component: () => import('/@/views/install/install-wizard.vue'),
  meta: {
    title: '系统安装',
    hideInMenu: true,
  },
};
