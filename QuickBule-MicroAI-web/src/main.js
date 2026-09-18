/*
 * 项目启动入口方法
 *

 */

import { createApp } from 'vue';
import Antd, { message } from 'ant-design-vue';
import * as antIcons from '@ant-design/icons-vue';
import lodash from 'lodash';
import JsonViewer from 'vue3-json-viewer';
import 'vue3-json-viewer/dist/vue3-json-viewer.css';
import App from './App.vue';
import { sentry } from '/@/lib/sentry';
import { loginApi } from '/@/api/system/login-api';
import constantsInfo from '/@/constants/index';
import { privilegeDirective } from '/@/directives/privilege';
import i18n from '/@/i18n/index';
import privilegePlugin from '/@/plugins/privilege-plugin';
import dictPlugin from '/@/plugins/dict-plugin';
import enumPlugin from '/@/plugins/enums-plugin';
import { buildRoutes, router } from '/@/router';
import { store } from '/@/store';
import { useUserStore } from '/@/store/modules/system/user';
import 'ant-design-vue/dist/reset.css';
import '/@/theme/index.less';
import { localRead } from '/@/utils/local-util.js';
import LocalStorageKeyConst from '/@/constants/local-storage-key-const.js';
import '/@/utils/ployfill';
import { useDictStore } from '/@/store/modules/system/dict.js';
import { dictApi } from '/@/api/support/dict-api.js';
import * as Icons from '@ant-design/icons-vue';

const app = createApp(App);

// 或者注册常用图标
app.component('CheckCircleOutlined', Icons.CheckCircleOutlined);
app.component('CloseCircleOutlined', Icons.CloseCircleOutlined);
app.component('LineChartOutlined', Icons.LineChartOutlined);
app.component('PieChartOutlined', Icons.PieChartOutlined);
app.component('AreaChartOutlined', Icons.AreaChartOutlined);
app.component('TrophyOutlined', Icons.TrophyOutlined);
app.component('FileTextOutlined', Icons.FileTextOutlined);
app.component('ReloadOutlined', Icons.ReloadOutlined);
app.component('CaretUpOutlined', Icons.CaretUpOutlined);
app.component('CaretDownOutlined', Icons.CaretDownOutlined);
app.component('WarningOutlined', Icons.WarningOutlined);
app.component('HomeOutlined', Icons.HomeOutlined);
app.component('ContainerOutlined', Icons.ContainerOutlined);
app.component('BarChartOutlined', Icons.BarChartOutlined);
app.component('SettingOutlined', Icons.SettingOutlined);
/*
 * -------------------- ※ 着重 解释说明下main.js的初始化逻辑 begin ※ --------------------
 *
 * 1、在main.js里很多框架都是 直接调用初始化的vue方法，创建vue实例，然后挂载路由router、状态管理store等等，但是关于router这块是有问题的；
 * 2、因为现在大部分路由都是从后端接口返回的，如若直接初始化挂载路由，这时前端还没有从后端请求路由的数据，所以只能写到路由拦截器里，这样很绕很不清晰；
 *    正确的做法流程应该是：
 *      2.1）如果存在登录信息，则先ajax请求用户的所有路由，然后加载，再去创建vue实例和挂载路由
 *      2.2）如果不存在路由信息，则创建vue实例和挂载路由（此时的路由应该只有login页面，因为用户拥有哪些路由是登录之后才知道的）
 * 3、以上，在main.js里两个方法，一个是 获取登录信息getLoginInfo，另一个初始化vue: initVue，在最下的if操作里
 *
 * -------------------- ※ 着重 解释说明下main.js的初始化逻辑 end ※ --------------------
 */

/**
 * 获取用户信息和用户权限对应的路由，构建动态路由
 */
async function getLoginInfo() {
  try {
    //获取登录用户信息
    const res = await loginApi.getLoginInfo();
    const dictRes = await dictApi.getAllDictData();
    //构建系统的路由
    let menuRouterList = res.data.menuList.filter((e) => e.path || e.frameUrl);
    buildRoutes(menuRouterList);
    initVue();
    // 初始化数据字典
    useDictStore().initData(dictRes.data);
    //更新用户信息到pinia
    useUserStore().setUserLoginInfo(res.data);
  } catch (e) {
    // axios拦截器已经统一处理了所有错误消息显示，这里不再重复显示
    sentry.captureError(e);
    initVue();
  }
}

async function initVue() {
  let vueApp = createApp(App);
  let app = vueApp
    .use(router)
    .use(store)
    .use(i18n)
    .use(Antd)
    .use(enumPlugin, constantsInfo)
    .use(privilegePlugin)
    .use(dictPlugin)
    .use(JsonViewer);
  //注入权限
  app.directive('privilege', {
    mounted(el, binding) {
      privilegeDirective(el, binding);
    },
  });
  // 注册图标组件
  Object.keys(antIcons).forEach((key) => {
    app.component(key, antIcons[key]);
  });
  //全局
  app.config.globalProperties.$antIcons = antIcons;
  app.config.globalProperties.$lodash = lodash;
  //挂载
  app.mount('#app');
}
//不需要获取用户信息、用户菜单、用户菜单动态路由，直接初始化vue即可
let token = localRead(LocalStorageKeyConst.USER_TOKEN);
if (!token) {
  await initVue();
} else {
  await getLoginInfo();
}
