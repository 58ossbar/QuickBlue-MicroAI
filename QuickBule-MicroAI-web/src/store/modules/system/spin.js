/*
 * loading
 *

 */
import { defineStore } from 'pinia';
import { sentry } from '/@/lib/sentry.js';

export const useSpinStore = defineStore({
  id: 'spin',
  state: () => ({
    loading: false,
  }),

  actions: {
    hide() {
      this.loading = false;
      // 安全的DOM操作，避免null引用错误
      try {
        const spins = document.querySelector('.ant-spin-nested-loading');
        if (spins) {
          spins.style.zIndex = '999';
        }
      } catch (error) {
        sentry.captureError('Spin hide操作失败:', error);
      }
    },
    show() {
      this.loading = true;
      // 安全的DOM操作，避免null引用错误
      try {
        const spins = document.querySelector('.ant-spin-nested-loading');
        if (spins) {
          spins.style.zIndex = '1001';
        }
      } catch (error) {
        sentry.captureError('Spin hide操作失败:', error);
      }
    },
  },
});
