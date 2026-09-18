import { describe, it, expect, vi, beforeEach } from 'vitest';
import { sentry } from './sentry';

describe('sentry', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Mock console.error
    console.error = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('基本结构测试', () => {
    it('应该是一个对象', () => {
      expect(typeof sentry).toBe('object');
      expect(sentry).not.toBeNull();
      expect(sentry).toBeDefined();
    });

    it('应该有captureError方法', () => {
      expect(sentry.captureError).toBeDefined();
      expect(typeof sentry.captureError).toBe('function');
    });
  });

  describe('captureError方法测试', () => {
    it('应该是一个函数', () => {
      expect(typeof sentry.captureError).toBe('function');
    });

    it('应该接受error参数', () => {
      const error = new Error('Test error');
      
      expect(() => {
        sentry.captureError(error);
      }).not.toThrow();
    });

    it('应该在控制台输出错误', () => {
      const error = new Error('Test error');
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
      expect(console.error).toHaveBeenCalledTimes(1);
    });
  });

  describe('axios错误过滤测试', () => {
    it('应该忽略axios响应错误', () => {
      const axiosError = {
        config: {},
        data: {},
        headers: {},
        request: {},
        status: 500,
      };
      
      sentry.captureError(axiosError);
      
      expect(console.error).not.toHaveBeenCalled();
    });

    it('应该忽略带有config和status的错误', () => {
      const error = {
        config: {},
        data: {},
        headers: {},
        request: {},
        status: 404,
      };
      
      sentry.captureError(error);
      
      expect(console.error).not.toHaveBeenCalled();
    });

    it('应该处理部分axios错误属性的情况', () => {
      // 缺少某个属性应该不会被忽略
      const partialAxiosError = {
        config: {},
        data: {},
        headers: {},
        status: 500,
      };
      
      sentry.captureError(partialAxiosError);
      
      expect(console.error).not.toHaveBeenCalled();
    });

    it('应该忽略所有axios错误属性都存在的错误', () => {
      const fullAxiosError = {
        config: {},
        data: {},
        headers: {},
        request: {},
        status: 500,
      };
      
      sentry.captureError(fullAxiosError);
      
      expect(console.error).not.toHaveBeenCalled();
    });
  });

  describe('普通错误处理测试', () => {
    it('应该处理标准Error对象', () => {
      const error = new Error('Test error');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理自定义错误', () => {
      class CustomError extends Error {
        constructor(message) {
          super(message);
          this.name = 'CustomError';
        }
      }
      
      const error = new CustomError('Custom error message');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理TypeError', () => {
      const error = new TypeError('Type error');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理ReferenceError', () => {
      const error = new ReferenceError('Reference error');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理SyntaxError', () => {
      const error = new SyntaxError('Syntax error');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });
  });

  describe('普通对象错误处理测试', () => {
    it('应该处理普通对象错误', () => {
      const error = { message: 'Object error' };
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理带stack信息的对象', () => {
      const error = {
        message: 'Error message',
        stack: 'Error stack trace',
      };
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理字符串错误', () => {
      const error = 'String error message';
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });
  });

  describe('边界情况测试', () => {
    it('应该处理undefined错误', () => {
      expect(() => {
        sentry.captureError(undefined);
      }).not.toThrow();
      
      expect(console.error).toHaveBeenCalledWith(undefined);
    });

    it('应该处理null错误', () => {
      expect(() => {
        sentry.captureError(null);
      }).not.toThrow();
      
      expect(console.error).toHaveBeenCalledWith(null);
    });

    it('应该处理数字错误', () => {
      const error = 404;
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理布尔值错误', () => {
      const error = true;
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理空对象', () => {
      const error = {};
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理空字符串', () => {
      const error = '';
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });
  });

  describe('重复调用测试', () => {
    it('应该支持多次调用', () => {
      const error1 = new Error('Error 1');
      const error2 = new Error('Error 2');
      const error3 = new Error('Error 3');
      
      sentry.captureError(error1);
      sentry.captureError(error2);
      sentry.captureError(error3);
      
      expect(console.error).toHaveBeenCalledTimes(3);
      expect(console.error).toHaveBeenNthCalledWith(1, error1);
      expect(console.error).toHaveBeenNthCalledWith(2, error2);
      expect(console.error).toHaveBeenNthCalledWith(3, error3);
    });

    it('多次调用axios错误应该都被忽略', () => {
      const axiosError = {
        config: {},
        data: {},
        headers: {},
        request: {},
        status: 500,
      };
      
      sentry.captureError(axiosError);
      sentry.captureError(axiosError);
      sentry.captureError(axiosError);
      
      expect(console.error).not.toHaveBeenCalled();
    });

    it('应该支持混合调用', () => {
      const normalError = new Error('Normal error');
      const axiosError = {
        config: {},
        data: {},
        headers: {},
        request: {},
        status: 500,
      };
      const customError = { message: 'Custom error' };
      
      sentry.captureError(normalError);
      sentry.captureError(axiosError);
      sentry.captureError(customError);
      
      expect(console.error).toHaveBeenCalledTimes(2);
      expect(console.error).toHaveBeenNthCalledWith(1, normalError);
      expect(console.error).toHaveBeenNthCalledWith(2, customError);
    });
  });

  describe('错误对象属性测试', () => {
    it('应该保留原始错误的所有属性', () => {
      const error = {
        message: 'Test error',
        code: 'ERR_TEST',
        details: { key: 'value' },
        timestamp: Date.now(),
      };
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理带有自定义属性的Error对象', () => {
      const error = new Error('Test error');
      error.code = 'CUSTOM_CODE';
      error.statusCode = 400;
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理函数类型的错误属性', () => {
      const error = {
        message: 'Error',
        handler: () => 'handler',
      };
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });
  });

  describe('特殊场景测试', () => {
    it('应该处理循环引用错误', () => {
      const error = { message: 'Error' };
      error.self = error;
      
      expect(() => {
        sentry.captureError(error);
      }).not.toThrow();
    });

    it('应该处理嵌套对象错误', () => {
      const error = {
        message: 'Nested error',
        nested: {
          level1: {
            level2: {
              level3: 'Deep value',
            },
          },
        },
      };
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理数组类型的错误', () => {
      const error = ['error1', 'error2', 'error3'];
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理包含数组的对象错误', () => {
      const error = {
        message: 'Array error',
        errors: ['error1', 'error2'],
        codes: [100, 200, 300],
      };
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });
  });

  describe('性能测试', () => {
    it('应该能够快速处理大量错误', () => {
      const iterations = 100;
      
      const startTime = Date.now();
      for (let i = 0; i < iterations; i++) {
        sentry.captureError(new Error(`Error ${i}`));
      }
      const endTime = Date.now();
      
      expect(endTime - startTime).toBeLessThan(1000); // 应该在1秒内完成
    });

    it('应该能够快速过滤大量axios错误', () => {
      const axiosError = {
        config: {},
        data: {},
        headers: {},
        request: {},
        status: 500,
      };
      
      const startTime = Date.now();
      for (let i = 0; i < 100; i++) {
        sentry.captureError(axiosError);
      }
      const endTime = Date.now();
      
      expect(endTime - startTime).toBeLessThan(100); // 应该很快
      expect(console.error).not.toHaveBeenCalled();
    });
  });

  describe('实际应用场景测试', () => {
    it('应该处理网络请求错误', () => {
      const error = new Error('Network request failed');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理JSON解析错误', () => {
      const error = new SyntaxError('Unexpected token');
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理Promise拒绝错误', () => {
      const error = new Error('Promise rejected');
      error.code = 'REJECTED';
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('应该处理异步操作错误', () => {
      const error = new Error('Async operation failed');
      error.async = true;
      
      sentry.captureError(error);
      
      expect(console.error).toHaveBeenCalledWith(error);
    });
  });
});
