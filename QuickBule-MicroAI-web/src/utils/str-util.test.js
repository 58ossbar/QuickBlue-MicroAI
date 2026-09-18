import { describe, it, expect } from 'vitest';
import { convertLowerHyphen, convertUpperCamel, convertLowerCamel } from './str-util';

describe('str-util.js', () => {
  describe('convertLowerHyphen', () => {
    it('应该将驼峰转为小写中划线', () => {
      expect(convertLowerHyphen('MyComponent')).toBe('my-component');
    });

    it('应该处理单个大写字母', () => {
      expect(convertLowerHyphen('A')).toBe('a');
    });

    it('应该处理连续大写字母', () => {
      expect(convertLowerHyphen('XMLParser')).toBe('x-m-l-parser');
    });

    it('应该处理已有中划线的字符串', () => {
      expect(convertLowerHyphen('my-component')).toBe('my-component');
    });

    it('应该处理空字符串', () => {
      expect(convertLowerHyphen('')).toBe('');
    });

    it('应该处理 null', () => {
      expect(convertLowerHyphen(null)).toBe('');
    });

    it('应该处理 undefined', () => {
      expect(convertLowerHyphen(undefined)).toBe('');
    });

    it('应该处理小写开头的驼峰', () => {
      expect(convertLowerHyphen('myComponent')).toBe('my-component');
    });

    it('应该处理全大写字符串', () => {
      expect(convertLowerHyphen('ABC')).toBe('-a-b-c');
    });

    it('应该处理数字', () => {
      expect(convertLowerHyphen('Test123')).toBe('test123');
    });
  });

  describe('convertUpperCamel', () => {
    it('应该将下划线转为大驼峰', () => {
      expect(convertUpperCamel('my_component')).toBe('MyComponent');
    });

    it('应该处理单个下划线', () => {
      expect(convertUpperCamel('_test')).toBe('Test');
    });

    it('应该处理连续下划线', () => {
      expect(convertUpperCamel('my__component')).toBe('My_component');
    });

    it('应该处理已经是驼峰的字符串', () => {
      expect(convertUpperCamel('myComponent')).toBe('MyComponent');
    });

    it('应该处理空字符串', () => {
      expect(convertUpperCamel('')).toBe('');
    });

    it('应该处理 null', () => {
      expect(convertUpperCamel(null)).toBe('');
    });

    it('应该处理 undefined', () => {
      expect(convertUpperCamel(undefined)).toBe('');
    });

    it('应该处理数字', () => {
      expect(convertUpperCamel('test_123')).toBe('Test123');
    });

    it('应该处理下划线开头', () => {
      expect(convertUpperCamel('_private_var')).toBe('_private_var');
    });

    it('应该处理多个单词', () => {
      expect(convertUpperCamel('my_test_component')).toBe('MyTestComponent');
    });
  });

  describe('convertLowerCamel', () => {
    it('应该将下划线转为小驼峰', () => {
      expect(convertLowerCamel('my_component')).toBe('myComponent');
    });

    it('应该处理单个下划线', () => {
      expect(convertLowerCamel('_test')).toBe('Test');
    });

    it('应该处理连续下划线', () => {
      expect(convertLowerCamel('my__component')).toBe('My_component');
    });

    it('应该处理已经是驼峰的字符串', () => {
      expect(convertLowerCamel('myComponent')).toBe('myComponent');
    });

    it('应该处理空字符串', () => {
      expect(convertLowerCamel('')).toBe('');
    });

    it('应该处理 null', () => {
      expect(convertLowerCamel(null)).toBe('');
    });

    it('应该处理 undefined', () => {
      expect(convertLowerCamel(undefined)).toBe('');
    });

    it('应该处理数字', () => {
      expect(convertLowerCamel('test_123')).toBe('Test123');
    });

    it('应该处理下划线开头', () => {
      expect(convertLowerCamel('_private_var')).toBe('_private_var');
    });

    it('应该处理多个单词', () => {
      expect(convertLowerCamel('my_test_component')).toBe('myTestComponent');
    });
  });
});
