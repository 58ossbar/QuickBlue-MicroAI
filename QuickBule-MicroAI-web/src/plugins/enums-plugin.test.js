import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import enumPlugin from './enums-plugin';

describe('enums-plugin', () => {
  let mockApp;
  let enumWrapper;

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock app instance
    mockApp = {
      config: {
        globalProperties: {},
      },
      provide: vi.fn(),
    };

    // Mock enumWrapper
    enumWrapper = {
      TEST_ENUM: {
        ACTIVE: { value: 1, desc: '激活' },
        INACTIVE: { value: 0, desc: '未激活' },
      },
      FLAG_NUMBER_ENUM: {
        TRUE: { value: 1, desc: '是' },
        FALSE: { value: 0, desc: '否' },
      },
    };

    // Mock console.error
    console.error = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('插件结构测试', () => {
    it('应该是一个对象', () => {
      expect(typeof enumPlugin).toBe('object');
      expect(enumPlugin).not.toBeNull();
    });

    it('应该有install方法', () => {
      expect(enumPlugin.install).toBeDefined();
      expect(typeof enumPlugin.install).toBe('function');
    });

    it('应该接受app和enumWrapper参数', () => {
      expect(() => {
        enumPlugin.install(mockApp, enumWrapper);
      }).not.toThrow();
    });

    it('应该在app.config.globalProperties上注册插件', () => {
      enumPlugin.install(mockApp, enumWrapper);
      
      expect(mockApp.config.globalProperties.$enumPlugin).toBeDefined();
      expect(typeof mockApp.config.globalProperties.$enumPlugin).toBe('object');
    });

    it('应该在app.provide上注册插件', () => {
      enumPlugin.install(mockApp, enumWrapper);
      
      expect(mockApp.provide).toHaveBeenCalledWith('enumPlugin', expect.any(Object));
      expect(mockApp.provide).toHaveBeenCalledTimes(1);
    });
  });

  describe('getDescByValue方法测试', () => {
    beforeEach(() => {
      enumPlugin.install(mockApp, enumWrapper);
    });

    it('应该根据枚举值获取描述', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 1);
      
      expect(result).toBe('激活');
    });

    it('应该正确处理枚举值0', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 0);
      
      expect(result).toBe('未激活');
    });

    it('当枚举名不存在时应该返回空字符串', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('NON_EXISTENT_ENUM', 1);
      
      expect(result).toBe('');
      expect(console.error).toHaveBeenCalled();
    });

    it('当枚举值不存在时应该返回空字符串', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 999);
      
      expect(result).toBe('');
    });

    it('应该处理布尔值类型（FLAG_NUMBER_ENUM）', () => {
      const trueResult = mockApp.config.globalProperties.$enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', true);
      const falseResult = mockApp.config.globalProperties.$enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', false);
      
      expect(trueResult).toBe('是');
      expect(falseResult).toBe('否');
    });

    it('应该处理数字值类型', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', 1);
      
      expect(result).toBe('是');
    });

    it('应该处理空字符串的枚举值', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', '');
      
      expect(result).toBe('');
    });

    it('应该处理null和undefined的枚举值', () => {
      const nullResult = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', null);
      const undefinedResult = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', undefined);
      
      expect(nullResult).toBe('');
      expect(undefinedResult).toBe('');
    });
  });

  describe('getValueDescList方法测试', () => {
    beforeEach(() => {
      enumPlugin.install(mockApp, enumWrapper);
    });

    it('应该根据枚举名获取值描述列表', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDescList('TEST_ENUM');
      
      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBe(2);
      expect(result).toContainEqual({ value: 1, desc: '激活' });
      expect(result).toContainEqual({ value: 0, desc: '未激活' });
    });

    it('返回的列表应该包含所有枚举项', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDescList('TEST_ENUM');
      
      expect(result.length).toBe(Object.keys(enumWrapper.TEST_ENUM).length);
    });

    it('当枚举名不存在时应该返回空数组', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDescList('NON_EXISTENT_ENUM');
      
      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBe(0);
      expect(console.error).toHaveBeenCalled();
    });

    it('返回的每一项都应该有value和desc属性', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDescList('TEST_ENUM');
      
      result.forEach(item => {
        expect(item).toHaveProperty('value');
        expect(item).toHaveProperty('desc');
      });
    });

    it('应该处理包含多个枚举项的枚举', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDescList('FLAG_NUMBER_ENUM');
      
      expect(result.length).toBe(2);
      expect(result).toContainEqual({ value: 1, desc: '是' });
      expect(result).toContainEqual({ value: 0, desc: '否' });
    });
  });

  describe('getValueDesc方法测试', () => {
    beforeEach(() => {
      enumPlugin.install(mockApp, enumWrapper);
    });

    it('应该根据枚举名获取值描述键值对对象', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDesc('TEST_ENUM');
      
      expect(typeof result).toBe('object');
      expect(result).not.toBeNull();
    });

    it('返回的对象应该包含所有枚举值作为键', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDesc('TEST_ENUM');
      
      expect(result['1']).toBe('激活');
      expect(result['0']).toBe('未激活');
    });

    it('键应该转换为字符串', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDesc('TEST_ENUM');
      
      expect(typeof Object.keys(result)[0]).toBe('string');
    });

    it('当枚举名不存在时应该返回空对象', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDesc('NON_EXISTENT_ENUM');
      
      expect(typeof result).toBe('object');
      expect(Object.keys(result).length).toBe(0);
      expect(console.error).toHaveBeenCalled();
    });

    it('应该正确处理数值键', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDesc('TEST_ENUM');
      
      expect(result['1']).toBe('激活');
      expect(result['0']).toBe('未激活');
    });

    it('应该包含所有枚举项', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getValueDesc('TEST_ENUM');
      
      expect(Object.keys(result).length).toBe(Object.keys(enumWrapper.TEST_ENUM).length);
    });
  });

  describe('综合测试', () => {
    beforeEach(() => {
      enumPlugin.install(mockApp, enumWrapper);
    });

    it('所有方法应该正确处理相同的枚举', () => {
      const desc = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 1);
      const list = mockApp.config.globalProperties.$enumPlugin.getValueDescList('TEST_ENUM');
      const obj = mockApp.config.globalProperties.$enumPlugin.getValueDesc('TEST_ENUM');
      
      expect(desc).toBe('激活');
      expect(list.some(item => item.value === 1 && item.desc === '激活')).toBe(true);
      expect(obj['1']).toBe('激活');
    });

    it('应该支持多次调用', () => {
      const result1 = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 1);
      const result2 = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 0);
      const result3 = mockApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 1);
      
      expect(result1).toBe('激活');
      expect(result2).toBe('未激活');
      expect(result3).toBe('激活');
    });
  });

  describe('边界情况测试', () => {
    beforeEach(() => {
      enumPlugin.install(mockApp, enumWrapper);
    });

    it('应该处理空的enumWrapper', () => {
      const emptyApp = {
        config: { globalProperties: {} },
        provide: vi.fn(),
      };
      
      expect(() => {
        enumPlugin.install(emptyApp, {});
      }).not.toThrow();
      
      const result = emptyApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 1);
      expect(result).toBe('');
    });

    it('应该处理null和undefined的enumWrapper', () => {
      const emptyApp = {
        config: { globalProperties: {} },
        provide: vi.fn(),
      };
      
      expect(() => {
        enumPlugin.install(emptyApp, null);
      }).not.toThrow();
      
      const result = emptyApp.config.globalProperties.$enumPlugin.getDescByValue('TEST_ENUM', 1);
      expect(result).toBe('');
    });

    it('应该处理空字符串的枚举名', () => {
      const result = mockApp.config.globalProperties.$enumPlugin.getDescByValue('', 1);
      
      expect(result).toBe('');
      expect(console.error).toHaveBeenCalled();
    });

    it('应该处理特殊的枚举值（负数、大数等）', () => {
      const specialEnumWrapper = {
        SPECIAL_ENUM: {
          NEGATIVE: { value: -1, desc: '负数' },
          LARGE: { value: 999999, desc: '大数' },
        },
      };
      
      const specialApp = {
        config: { globalProperties: {} },
        provide: vi.fn(),
      };
      
      enumPlugin.install(specialApp, specialEnumWrapper);
      
      const negResult = specialApp.config.globalProperties.$enumPlugin.getDescByValue('SPECIAL_ENUM', -1);
      const largeResult = specialApp.config.globalProperties.$enumPlugin.getDescByValue('SPECIAL_ENUM', 999999);
      
      expect(negResult).toBe('负数');
      expect(largeResult).toBe('大数');
    });
  });

  describe('错误处理测试', () => {
    beforeEach(() => {
      enumPlugin.install(mockApp, enumWrapper);
    });

    it('应该正确记录错误信息', () => {
      mockApp.config.globalProperties.$enumPlugin.getDescByValue('NON_EXISTENT_ENUM', 1);
      
      expect(console.error).toHaveBeenCalledWith(
        expect.stringContaining('无法找到变量名称'),
        expect.stringContaining('NON_EXISTENT_ENUM')
      );
    });

    it('getValueDescList应该正确记录错误信息', () => {
      mockApp.config.globalProperties.$enumPlugin.getValueDescList('NON_EXISTENT_ENUM');
      
      expect(console.error).toHaveBeenCalledWith(
        expect.stringContaining('无法找到变量名称'),
        expect.stringContaining('NON_EXISTENT_ENUM')
      );
    });

    it('getValueDesc应该正确记录错误信息', () => {
      mockApp.config.globalProperties.$enumPlugin.getValueDesc('NON_EXISTENT_ENUM');
      
      expect(console.error).toHaveBeenCalledWith(
        expect.stringContaining('无法找到变量名称'),
        expect.stringContaining('NON_EXISTENT_ENUM')
      );
    });
  });

  describe('实际应用场景测试', () => {
    it('应该支持动态枚举配置', () => {
      const dynamicEnumWrapper = {
        DYNAMIC_ENUM: {
          OPTION_A: { value: 'A', desc: '选项A' },
          OPTION_B: { value: 'B', desc: '选项B' },
        },
      };
      
      const dynamicApp = {
        config: { globalProperties: {} },
        provide: vi.fn(),
      };
      
      enumPlugin.install(dynamicApp, dynamicEnumWrapper);
      
      const desc = dynamicApp.config.globalProperties.$enumPlugin.getDescByValue('DYNAMIC_ENUM', 'A');
      expect(desc).toBe('选项A');
    });

    it('应该支持包含特殊字符的描述', () => {
      const specialDescEnumWrapper = {
        SPECIAL_DESC_ENUM: {
          OPTION_A: { value: 1, desc: '选项A（包含特殊字符）' },
          OPTION_B: { value: 2, desc: '选项B@#$%^' },
        },
      };
      
      const specialDescApp = {
        config: { globalProperties: {} },
        provide: vi.fn(),
      };
      
      enumPlugin.install(specialDescApp, specialDescEnumWrapper);
      
      const desc = specialDescApp.config.globalProperties.$enumPlugin.getDescByValue('SPECIAL_DESC_ENUM', 1);
      expect(desc).toBe('选项A（包含特殊字符）');
    });

    it('应该支持包含中文的枚举值和描述', () => {
      const chineseEnumWrapper = {
        CHINESE_ENUM: {
          选项一: { value: 1, desc: '选项一描述' },
          选项二: { value: 2, desc: '选项二描述' },
        },
      };
      
      const chineseApp = {
        config: { globalProperties: {} },
        provide: vi.fn(),
      };
      
      enumPlugin.install(chineseApp, chineseEnumWrapper);
      
      const list = chineseApp.config.globalProperties.$enumPlugin.getValueDescList('CHINESE_ENUM');
      expect(list.length).toBe(2);
    });
  });

  describe('插件注册测试', () => {
    it('应该在全局属性上正确注册插件', () => {
      enumPlugin.install(mockApp, enumWrapper);
      
      expect(mockApp.config.globalProperties.$enumPlugin).toBeDefined();
      expect(mockApp.config.globalProperties.$enumPlugin.getDescByValue).toBeDefined();
      expect(mockApp.config.globalProperties.$enumPlugin.getValueDescList).toBeDefined();
      expect(mockApp.config.globalProperties.$enumPlugin.getValueDesc).toBeDefined();
    });

    it('应该在provide中正确注册插件', () => {
      enumPlugin.install(mockApp, enumWrapper);
      
      expect(mockApp.provide).toHaveBeenCalledWith('enumPlugin', expect.any(Object));
    });

    it('应该能够通过provide访问插件方法', () => {
      enumPlugin.install(mockApp, enumWrapper);
      
      const plugin = mockApp.provide.mock.calls[0][1];
      expect(plugin.getDescByValue).toBeDefined();
      expect(plugin.getValueDescList).toBeDefined();
      expect(plugin.getValueDesc).toBeDefined();
    });
  });
});
