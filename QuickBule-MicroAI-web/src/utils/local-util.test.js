import { describe, it, expect, vi, beforeEach } from 'vitest';
import { localSave, localRead, localClear, localRemove } from './local-util';

describe('local-util', () => {
  beforeEach(() => {
    // 清除所有localStorage的mock
    vi.clearAllMocks();
  });

  describe('localSave', () => {
    it('应该正确保存数据到localStorage', () => {
      const key = 'testKey';
      const value = 'testValue';
      
      localSave(key, value);
      
      expect(localStorage.setItem).toHaveBeenCalledWith(key, value);
      expect(localStorage.setItem).toHaveBeenCalledTimes(1);
    });

    it('应该保存不同类型的值', () => {
      localSave('string', 'hello');
      localSave('number', '123');
      localSave('boolean', 'true');
      localSave('object', '{"name":"test"}');
      
      expect(localStorage.setItem).toHaveBeenCalledTimes(4);
      expect(localStorage.setItem).toHaveBeenCalledWith('string', 'hello');
      expect(localStorage.setItem).toHaveBeenCalledWith('number', '123');
      expect(localStorage.setItem).toHaveBeenCalledWith('boolean', 'true');
      expect(localStorage.setItem).toHaveBeenCalledWith('object', '{"name":"test"}');
    });

    it('应该保存空字符串', () => {
      localSave('empty', '');
      
      expect(localStorage.setItem).toHaveBeenCalledWith('empty', '');
    });

    it('应该保存包含特殊字符的值', () => {
      const specialValue = 'test@#$%^&*()_+-={}[]|\\:";\'<>?,./`~';
      localSave('special', specialValue);
      
      expect(localStorage.setItem).toHaveBeenCalledWith('special', specialValue);
    });

    it('应该保存超长字符串', () => {
      const longValue = 'a'.repeat(10000);
      localSave('long', longValue);
      
      expect(localStorage.setItem).toHaveBeenCalledWith('long', longValue);
    });

    it('应该保存null和undefined转换后的字符串', () => {
      localSave('null', 'null');
      localSave('undefined', 'undefined');
      
      expect(localStorage.setItem).toHaveBeenCalledWith('null', 'null');
      expect(localStorage.setItem).toHaveBeenCalledWith('undefined', 'undefined');
    });
  });

  describe('localRead', () => {
    it('应该正确从localStorage读取数据', () => {
      localStorage.getItem.mockReturnValue('testValue');
      
      const result = localRead('testKey');
      
      expect(localStorage.getItem).toHaveBeenCalledWith('testKey');
      expect(result).toBe('testValue');
    });

    it('当localStorage中不存在该key时应该返回空字符串', () => {
      localStorage.getItem.mockReturnValue(null);
      
      const result = localRead('nonExistentKey');
      
      expect(localStorage.getItem).toHaveBeenCalledWith('nonExistentKey');
      expect(result).toBe('');
    });

    it('当localStorage.getItem返回undefined时应该返回空字符串', () => {
      localStorage.getItem.mockReturnValue(undefined);
      
      const result = localRead('testKey');
      
      expect(result).toBe('');
    });

    it('应该读取不同类型的值', () => {
      localStorage.getItem.mockImplementation((key) => {
        const values = {
          'string': 'hello',
          'number': '123',
          'boolean': 'true',
          'object': '{"name":"test"}'
        };
        return values[key];
      });
      
      expect(localRead('string')).toBe('hello');
      expect(localRead('number')).toBe('123');
      expect(localRead('boolean')).toBe('true');
      expect(localRead('object')).toBe('{"name":"test"}');
    });

    it('应该读取空字符串', () => {
      localStorage.getItem.mockReturnValue('');
      
      const result = localRead('empty');
      
      expect(result).toBe('');
    });

    it('应该读取包含特殊字符的值', () => {
      const specialValue = 'test@#$%^&*()_+-={}[]|\\:";\'<>?,./`~';
      localStorage.getItem.mockReturnValue(specialValue);
      
      const result = localRead('special');
      
      expect(result).toBe(specialValue);
    });
  });

  describe('localClear', () => {
    it('应该清除localStorage中的所有数据', () => {
      localClear();
      
      expect(localStorage.clear).toHaveBeenCalledTimes(1);
      expect(localStorage.clear).toHaveBeenCalledWith();
    });

    it('多次调用localClear应该正常工作', () => {
      localClear();
      localClear();
      localClear();
      
      expect(localStorage.clear).toHaveBeenCalledTimes(3);
    });
  });

  describe('localRemove', () => {
    it('应该正确删除localStorage中的指定key', () => {
      const key = 'testKey';
      
      localRemove(key);
      
      expect(localStorage.removeItem).toHaveBeenCalledWith(key);
      expect(localStorage.removeItem).toHaveBeenCalledTimes(1);
    });

    it('应该删除多个不同的key', () => {
      localRemove('key1');
      localRemove('key2');
      localRemove('key3');
      
      expect(localStorage.removeItem).toHaveBeenCalledTimes(3);
      expect(localStorage.removeItem).toHaveBeenCalledWith('key1');
      expect(localStorage.removeItem).toHaveBeenCalledWith('key2');
      expect(localStorage.removeItem).toHaveBeenCalledWith('key3');
    });

    it('应该删除包含特殊字符的key', () => {
      const specialKey = 'test@#$%^&*()_+-={}[]|\\:";\'<>?,./`~';
      
      localRemove(specialKey);
      
      expect(localStorage.removeItem).toHaveBeenCalledWith(specialKey);
    });

    it('应该删除不存在的key', () => {
      localRemove('nonExistentKey');
      
      expect(localStorage.removeItem).toHaveBeenCalledWith('nonExistentKey');
    });
  });

  describe('综合操作测试', () => {
    it('应该支持完整的读写删除流程', () => {
      // 保存
      localSave('test', 'value');
      expect(localStorage.setItem).toHaveBeenCalledWith('test', 'value');
      
      // 读取
      localStorage.getItem.mockReturnValue('value');
      const result = localRead('test');
      expect(result).toBe('value');
      
      // 删除
      localRemove('test');
      expect(localStorage.removeItem).toHaveBeenCalledWith('test');
    });

    it('应该支持批量操作', () => {
      // 批量保存
      localSave('key1', 'value1');
      localSave('key2', 'value2');
      localSave('key3', 'value3');
      
      expect(localStorage.setItem).toHaveBeenCalledTimes(3);
      
      // 批量读取
      localStorage.getItem.mockImplementation((key) => `value${key.slice(-1)}`);
      expect(localRead('key1')).toBe('value1');
      expect(localRead('key2')).toBe('value2');
      expect(localRead('key3')).toBe('value3');
      
      // 批量删除
      localRemove('key1');
      localRemove('key2');
      localRemove('key3');
      
      expect(localStorage.removeItem).toHaveBeenCalledTimes(3);
    });

    it('应该支持清空所有数据的场景', () => {
      // 保存多个数据
      localSave('key1', 'value1');
      localSave('key2', 'value2');
      localSave('key3', 'value3');
      
      // 清空所有数据
      localClear();
      
      expect(localStorage.clear).toHaveBeenCalled();
    });
  });

  describe('边界情况测试', () => {
    it('应该处理空字符串key', () => {
      localSave('', 'value');
      expect(localStorage.setItem).toHaveBeenCalledWith('', 'value');
      
      localStorage.getItem.mockReturnValue('value');
      expect(localRead('')).toBe('value');
      
      localRemove('');
      expect(localStorage.removeItem).toHaveBeenCalledWith('');
    });

    it('应该处理null和undefined作为key的情况', () => {
      localSave(null, 'value');
      expect(localStorage.setItem).toHaveBeenCalledWith(null, 'value');
      
      localSave(undefined, 'value');
      expect(localStorage.setItem).toHaveBeenCalledWith(undefined, 'value');
    });

    it('应该处理大量数据的性能场景', () => {
      const saveCount = 100;
      for (let i = 0; i < saveCount; i++) {
        localSave(`key${i}`, `value${i}`);
      }
      
      expect(localStorage.setItem).toHaveBeenCalledTimes(saveCount);
    });
  });
});
