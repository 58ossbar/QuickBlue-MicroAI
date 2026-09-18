/*
 * 系统更新日志
 *
 */
import ChangeLogList from '/@/views/support/change-log/change-log-list.vue';

export const changeLogRouters = [
  {
    path: '/support/change-log',
    name: 'ChangeLog',
    component: ChangeLogList,
    meta: {
      title: '系统更新日志',
    },
  },
];
