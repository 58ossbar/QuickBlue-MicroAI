import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useDictStore } from './dict';

// Mock sentry
vi.mock('/@/lib/sentry.js', () => ({
  sentry: {
    captureError: vi.fn(),
  },
}));

// Mock dictApi
vi.mock('/@/api/support/dict-api.js', () => ({
  dictApi: {
    getAllDictData: vi.fn(),
  },
}));

describe('dict store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('state', () => {
    it('应该初始化为空的状态', () => {
      const store = useDictStore();
      expect(store.dictList).toEqual([]);
      expect(store.dictMap).toBeInstanceOf(Map);
      expect(store.dictMap.size).toBe(0);
    });
  });

  describe('getDictList', () => {
    it('应该返回字典列表', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST1', dictName: '测试1', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST2', dictName: '测试2', dataValue: '2', dataLabel: '选项2' },
      ];
      store.initData(testData);
      expect(store.getDictList()).toEqual([
        { dictCode: 'TEST1', dictName: '测试1', disabledFlag: undefined },
        { dictCode: 'TEST2', dictName: '测试2', disabledFlag: undefined },
      ]);
    });
  });

  describe('getDictData', () => {
    it('应该根据 dictCode 返回对应的字典数据', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST1', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST1', dataValue: '2', dataLabel: '选项2' },
        { dictCode: 'TEST2', dataValue: '3', dataLabel: '选项3' },
      ];
      store.initData(testData);
      const result = store.getDictData('TEST1');
      expect(result).toEqual([
        { dictCode: 'TEST1', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST1', dataValue: '2', dataLabel: '选项2' },
      ]);
    });

    it('当 dictCode 不存在时应该返回空数组', () => {
      const store = useDictStore();
      const result = store.getDictData('NON_EXISTENT');
      expect(result).toEqual([]);
    });

    it('当 dictCode 为空时应该返回空数组', () => {
      const store = useDictStore();
      expect(store.getDictData('')).toEqual([]);
      expect(store.getDictData(null)).toEqual([]);
      expect(store.getDictData(undefined)).toEqual([]);
    });
  });

  describe('getDataLabels', () => {
    it('应该根据 dictCode 和 dataValue 返回对应的标签', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST', dataValue: '2', dataLabel: '选项2' },
      ];
      store.initData(testData);
      expect(store.getDataLabels('TEST', '1')).toBe('选项1');
    });

    it('应该处理数字类型的 dataValue', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST', dataValue: '2', dataLabel: '选项2' },
      ];
      store.initData(testData);
      expect(store.getDataLabels('TEST', 1)).toBe('选项1');
    });

    it('应该处理包含分隔符的 dataValue', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST', dataValue: '2', dataLabel: '选项2' },
        { dictCode: 'TEST', dataValue: '3', dataLabel: '选项3' },
      ];
      store.initData(testData);
      expect(store.getDataLabels('TEST', '1,2')).toBe('选项1,选项2');
    });

    it('当 dataValue 为 null 时应该返回空字符串', () => {
      const store = useDictStore();
      expect(store.getDataLabels('TEST', null)).toBe('');
    });

    it('当 dataValue 为 undefined 时应该返回空字符串', () => {
      const store = useDictStore();
      expect(store.getDataLabels('TEST', undefined)).toBe('');
    });

    it('当 dataValue 为 NaN 时应该返回空字符串', () => {
      const store = useDictStore();
      expect(store.getDataLabels('TEST', NaN)).toBe('');
    });

    it('当字典不存在时应该返回空字符串', () => {
      const store = useDictStore();
      expect(store.getDataLabels('NON_EXISTENT', '1')).toBe('');
    });

    it('当 dataValue 不匹配任何字典项时应该返回空字符串', () => {
      const store = useDictStore();
      const testData = [{ dictCode: 'TEST', dataValue: '1', dataLabel: '选项1' }];
      store.initData(testData);
      expect(store.getDataLabels('TEST', '999')).toBe('');
    });
  });

  describe('initData', () => {
    it('应该初始化字典数据', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST1', dictName: '测试1', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST2', dictName: '测试2', dataValue: '2', dataLabel: '选项2' },
      ];
      store.initData(testData);
      expect(store.dictList).toHaveLength(2);
      expect(store.dictMap.size).toBe(2);
    });

    it('应该清空之前的字典数据', () => {
      const store = useDictStore();
      const testData1 = [{ dictCode: 'TEST1', dataValue: '1', dataLabel: '选项1' }];
      store.initData(testData1);
      expect(store.dictList).toHaveLength(1);

      const testData2 = [{ dictCode: 'TEST2', dataValue: '2', dataLabel: '选项2' }];
      store.initData(testData2);
      expect(store.dictList).toHaveLength(1);
      expect(store.dictMap.get('TEST1')).toBeUndefined();
      expect(store.dictMap.get('TEST2')).toBeDefined();
    });

    it('应该正确处理相同 dictCode 的多条数据', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST', dataValue: '1', dataLabel: '选项1' },
        { dictCode: 'TEST', dataValue: '2', dataLabel: '选项2' },
        { dictCode: 'TEST', dataValue: '3', dataLabel: '选项3' },
      ];
      store.initData(testData);
      const dictData = store.getDictData('TEST');
      expect(dictData).toHaveLength(3);
    });

    it('应该保存 dictName 和 disabledFlag', () => {
      const store = useDictStore();
      const testData = [
        { dictCode: 'TEST', dictName: '测试字典', dictDisabledFlag: 1, dataValue: '1', dataLabel: '选项1' },
      ];
      store.initData(testData);
      expect(store.dictList[0]).toEqual({
        dictCode: 'TEST',
        dictName: '测试字典',
        disabledFlag: 1,
      });
    });

    it('应该处理空数组', () => {
      const store = useDictStore();
      store.initData([]);
      expect(store.dictList).toEqual([]);
      expect(store.dictMap.size).toBe(0);
    });
  });

  describe('refreshData', () => {
    it('应该调用 API 刷新字典数据', async () => {
      const { dictApi } = await import('/@/api/support/dict-api.js');
      const store = useDictStore();
      const mockData = [
        { dictCode: 'TEST', dataValue: '1', dataLabel: '选项1' },
      ];
      dictApi.getAllDictData.mockResolvedValue({ data: mockData });
      
      await store.refreshData();
      
      expect(dictApi.getAllDictData).toHaveBeenCalled();
      expect(store.getDictData('TEST')).toEqual(mockData);
    });

    it('当 API 调用失败时应该捕获错误', async () => {
      const { dictApi } = await import('/@/api/support/dict-api.js');
      const { sentry } = await import('/@/lib/sentry.js');
      const store = useDictStore();
      const error = new Error('API Error');
      dictApi.getAllDictData.mockRejectedValue(error);
      
      await store.refreshData();
      
      expect(sentry.captureError).toHaveBeenCalledWith(error);
    });
  });

  describe('集成测试', () => {
    it('完整的字典操作流程', () => {
      const store = useDictStore();
      
      // 1. 初始化数据
      const testData = [
        { dictCode: 'STATUS', dictName: '状态', dataValue: '1', dataLabel: '启用' },
        { dictCode: 'STATUS', dictName: '状态', dataValue: '0', dataLabel: '禁用' },
        { dictCode: 'TYPE', dictName: '类型', dataValue: 'A', dataLabel: '类型A' },
      ];
      store.initData(testData);
      
      // 2. 获取字典列表
      const dictList = store.getDictList();
      expect(dictList).toHaveLength(2);
      
      // 3. 获取特定字典数据
      const statusDict = store.getDictData('STATUS');
      expect(statusDict).toHaveLength(2);
      
      // 4. 获取标签
      const label1 = store.getDataLabels('STATUS', '1');
      expect(label1).toBe('启用');
      
      const label2 = store.getDataLabels('STATUS', 1);
      expect(label2).toBe('启用');
      
      const label3 = store.getDataLabels('STATUS', '1,0');
      expect(label3).toBe('启用,禁用');
    });
  });
});
