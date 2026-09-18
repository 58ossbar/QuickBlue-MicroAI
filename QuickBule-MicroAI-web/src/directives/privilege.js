/*
 * 权限
 *

 */

import { useUserStore } from '/@/store/modules/system/user';
import _ from 'lodash';

export function privilegeDirective(el, binding) {
  // 超级管理员
  if (useUserStore().administratorFlag) {
    return true;
  }
  // 获取功能点权限
  let userPointsList = useUserStore().getPointList;
  if (!userPointsList) {
    return false;
  }
  // 如果没有权限，删除节点
  if (!_.some(userPointsList, ['webPerms', binding.value])) {
    el.parentNode.removeChild(el);
  }
  return true;
}
