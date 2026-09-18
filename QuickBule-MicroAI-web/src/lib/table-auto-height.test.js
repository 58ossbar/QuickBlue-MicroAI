import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { calcTableHeight } from './table-auto-height';

// Mock the store
vi.mock('../store/modules/system/app-config', () => ({
  useAppConfigStore: () => ({
    $state: {
      pageTagFlag: false,
      footerFlag: false,
    },
  }),
}));

describe('table-auto-height', () => {
  beforeEach(() => {
    // Setup DOM environment
    document.body.innerHTML = `
      <div id="budaosLayoutContent">
        <div class="content">Test Content</div>
      </div>
    `;
    
    // Mock offsetHeight
    const layoutElement = document.querySelector('#budaosLayoutContent');
    Object.defineProperty(layoutElement, 'offsetHeight', {
      value: 800,
      configurable: true,
    });
  });

  afterEach(() => {
    document.body.innerHTML = '';
    vi.clearAllMocks();
  });

  describe('基本功能测试', () => {
    it('应该是一个导出的函数', () => {
      expect(typeof calcTableHeight).toBe('function');
    });

    it('应该接受三个参数', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      expect(() => {
        calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      }).not.toThrow();
    });

    it('应该更新heightRef的值', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBeDefined();
      expect(typeof heightRef.value).toBe('number');
    });
  });

  describe('默认行为测试', () => {
    it('没有额外高度和引用时应该正确计算高度', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      // 默认扣除40px
      expect(heightRef.value).toBe(760); // 800 - 40
    });

    it('应该从布局容器获取总高度', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      const layoutElement = document.querySelector('#budaosLayoutContent');
      Object.defineProperty(layoutElement, 'offsetHeight', {
        value: 1000,
        configurable: true,
      });
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(960); // 1000 - 40
    });
  });

  describe('removeRefArray参数测试', () => {
    it('应该计算removeRefArray中所有元素的高度', () => {
      const heightRef = { value: 0 };
      
      const mockRef1 = { value: { $el: { offsetHeight: 50 } } };
      const mockRef2 = { value: { $el: { offsetHeight: 30 } } };
      const removeRefArray = [mockRef1, mockRef2];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (50 + 30 + 40) = 680
      expect(heightRef.value).toBe(680);
    });

    it('应该处理空的removeRefArray', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(760);
    });

    it('应该处理null的removeRefArray', () => {
      const heightRef = { value: 0 };
      const removeRefArray = null;
      const extraRemoveHeight = 0;
      
      expect(() => {
        calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      }).not.toThrow();
      
      expect(heightRef.value).toBe(760);
    });

    it('应该处理多个ref元素的情况', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [
        { value: { $el: { offsetHeight: 20 } } },
        { value: { $el: { offsetHeight: 20 } } },
        { value: { $el: { offsetHeight: 20 } } },
        { value: { $el: { offsetHeight: 20 } } },
      ];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (20*4 + 40) = 680
      expect(heightRef.value).toBe(680);
    });
  });

  describe('extraRemoveHeight参数测试', () => {
    it('应该加上额外的移除高度', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 100;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(660); // 800 - 40 - 100
    });

    it('应该处理extraRemoveHeight为0的情况', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(760);
    });

    it('应该处理大的extraRemoveHeight值', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 500;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(260); // 800 - 40 - 500
    });
  });

  describe('store状态影响测试', () => {
    beforeEach(() => {
      vi.resetModules();
    });

    it('应该考虑pageTagFlag对高度的影响', () => {
      const { useAppConfigStore } = require('../store/modules/system/app-config');
      
      // Mock store with pageTagFlag true
      useAppConfigStore.mockReturnValue({
        $state: {
          pageTagFlag: true,
          footerFlag: false,
        },
      });
      
      const { calcTableHeight: calcHeightWithFlag } = require('./table-auto-height');
      
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcHeightWithFlag(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (40 + 40) = 720
      expect(heightRef.value).toBe(720);
    });

    it('应该考虑footerFlag对高度的影响', () => {
      const { useAppConfigStore } = require('../store/modules/system/app-config');
      
      // Mock store with footerFlag true
      useAppConfigStore.mockReturnValue({
        $state: {
          pageTagFlag: false,
          footerFlag: true,
        },
      });
      
      const { calcTableHeight: calcHeightWithFlag } = require('./table-auto-height');
      
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcHeightWithFlag(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (40 + 40) = 720
      expect(heightRef.value).toBe(720);
    });

    it('应该同时考虑pageTagFlag和footerFlag', () => {
      const { useAppConfigStore } = require('../store/modules/system/app-config');
      
      // Mock store with both flags true
      useAppConfigStore.mockReturnValue({
        $state: {
          pageTagFlag: true,
          footerFlag: true,
        },
      });
      
      const { calcTableHeight: calcHeightWithFlag } = require('./table-auto-height');
      
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      calcHeightWithFlag(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (40 + 40 + 40) = 680
      expect(heightRef.value).toBe(680);
    });
  });

  describe('综合场景测试', () => {
    it('应该正确处理所有参数组合', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [
        { value: { $el: { offsetHeight: 50 } } },
        { value: { $el: { offsetHeight: 30 } } },
      ];
      const extraRemoveHeight = 80;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (50 + 30 + 80 + 40) = 600
      expect(heightRef.value).toBe(600);
    });

    it('应该能够处理较大的布局高度', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      const layoutElement = document.querySelector('#budaosLayoutContent');
      Object.defineProperty(layoutElement, 'offsetHeight', {
        value: 2000,
        configurable: true,
      });
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(1960); // 2000 - 40
    });

    it('应该能够处理较小的布局高度', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      const layoutElement = document.querySelector('#budaosLayoutContent');
      Object.defineProperty(layoutElement, 'offsetHeight', {
        value: 100,
        configurable: true,
      });
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(60); // 100 - 40
    });
  });

  describe('边界情况测试', () => {
    it('应该处理offsetHeight为0的ref元素', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [
        { value: { $el: { offsetHeight: 0 } } },
      ];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(760);
    });

    it('应该处理所有offsetHeight为0的ref元素', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [
        { value: { $el: { offsetHeight: 0 } } },
        { value: { $el: { offsetHeight: 0 } } },
        { value: { $el: { offsetHeight: 0 } } },
      ];
      const extraRemoveHeight = 0;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(heightRef.value).toBe(760);
    });

    it('应该处理negative的extraRemoveHeight', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = -50;
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - 40 - (-50) = 810
      expect(heightRef.value).toBe(810);
    });

    it('应该处理计算结果为负数的情况', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [
        { value: { $el: { offsetHeight: 500 } } },
        { value: { $el: { offsetHeight: 500 } } },
      ];
      const extraRemoveHeight = 500;
      
      const layoutElement = document.querySelector('#budaosLayoutContent');
      Object.defineProperty(layoutElement, 'offsetHeight', {
        value: 800,
        configurable: true,
      });
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      // 800 - (500 + 500 + 500 + 40) = -740
      expect(heightRef.value).toBeLessThan(0);
    });
  });

  describe('DOM元素查找测试', () => {
    it('应该在正确的DOM元素上计算高度', () => {
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      const spy = vi.spyOn(document, 'querySelector');
      
      calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      
      expect(spy).toHaveBeenCalledWith('#budaosLayoutContent');
      
      spy.mockRestore();
    });
  });

  describe('错误处理测试', () => {
    it('当DOM元素不存在时应该优雅处理', () => {
      document.body.innerHTML = '';
      
      const heightRef = { value: 0 };
      const removeRefArray = [];
      const extraRemoveHeight = 0;
      
      expect(() => {
        calcTableHeight(heightRef, removeRefArray, extraRemoveHeight);
      }).toThrow();
    });
  });
});
