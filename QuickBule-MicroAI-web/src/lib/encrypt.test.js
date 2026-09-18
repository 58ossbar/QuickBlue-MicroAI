import { describe, it, expect, beforeEach } from 'vitest';
import { encryptData, decryptData } from './encrypt';

describe('encrypt', () => {
  beforeEach(() => {
    // 每个测试前清理
    vi.clearAllMocks();
  });

  describe('基本功能测试', () => {
    it('应该能够加密和解密字符串', () => {
      const originalText = 'Hello World';
      
      const encrypted = encryptData(originalText);
      expect(encrypted).not.toBe(originalText);
      expect(encrypted).toBeTruthy();
      
      const decrypted = decryptData(encrypted);
      expect(decrypted).toBe(originalText);
    });

    it('应该能够加密和解密空字符串', () => {
      const originalText = '';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够加密和解密中文字符串', () => {
      const originalText = '你好世界';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够加密和解密特殊字符', () => {
      const originalText = '!@#$%^&*()_+-={}[]|\\:";\'<>?,./`~';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够加密和解密数字字符串', () => {
      const originalText = '1234567890';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够加密和解密混合类型字符串', () => {
      const originalText = 'Hello123世界!@#';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });
  });

  describe('对象加密解密测试', () => {
    it('应该能够加密和解密简单对象', () => {
      const originalObject = { name: 'test', age: 25 };
      
      const encrypted = encryptData(originalObject);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(originalObject));
    });

    it('应该能够加密和解密嵌套对象', () => {
      const originalObject = {
        user: {
          name: 'test',
          profile: {
            age: 25,
            address: {
              city: 'Beijing',
              country: 'China'
            }
          }
        }
      };
      
      const encrypted = encryptData(originalObject);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(originalObject));
    });

    it('应该能够加密和解密数组', () => {
      const originalArray = [1, 2, 3, 'test', { key: 'value' }];
      
      const encrypted = encryptData(originalArray);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(originalArray));
    });

    it('应该能够加密和解密空对象', () => {
      const originalObject = {};
      
      const encrypted = encryptData(originalObject);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(originalObject));
    });

    it('应该能够加密和解密空数组', () => {
      const originalArray = [];
      
      const encrypted = encryptData(originalArray);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(originalArray));
    });
  });

  describe('边界情况测试', () => {
    it('当输入为null时应该返回null', () => {
      const result = encryptData(null);
      expect(result).toBeNull();
    });

    it('当输入为undefined时应该返回null', () => {
      const result = encryptData(undefined);
      expect(result).toBeNull();
    });

    it('当decryptData输入为null时应该返回null', () => {
      const result = decryptData(null);
      expect(result).toBeNull();
    });

    it('当decryptData输入为undefined时应该返回null', () => {
      const result = decryptData(undefined);
      expect(result).toBeNull();
    });

    it('应该能够加密和解密布尔值', () => {
      const originalBool = true;
      
      const encrypted = encryptData(originalBool);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe('true');
    });

    it('应该能够加密和解数字值', () => {
      const originalNumber = 123;
      
      const encrypted = encryptData(originalNumber);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe('123');
    });

    it('应该能够加密和解密零值', () => {
      const originalNumber = 0;
      
      const encrypted = encryptData(originalNumber);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe('0');
    });
  });

  describe('加密结果特性测试', () => {
    it('每次加密相同字符串应该得到相同的结果（ECB模式）', () => {
      const originalText = 'test';
      
      const encrypted1 = encryptData(originalText);
      const encrypted2 = encryptData(originalText);
      
      expect(encrypted1).toBe(encrypted2);
    });

    it('加密结果应该是Base64编码的字符串', () => {
      const originalText = 'test';
      const encrypted = encryptData(originalText);
      
      // 检查是否为有效的Base64字符串
      const base64Pattern = /^[A-Za-z0-9+/]+=*$/;
      expect(encrypted).toMatch(base64Pattern);
    });

    it('加密结果不应该包含原文', () => {
      const originalText = 'secretPassword';
      const encrypted = encryptData(originalText);
      
      expect(encrypted).not.toContain(originalText);
    });

    it('不同明文应该产生不同的密文', () => {
      const text1 = 'hello';
      const text2 = 'world';
      
      const encrypted1 = encryptData(text1);
      const encrypted2 = encryptData(text2);
      
      expect(encrypted1).not.toBe(encrypted2);
    });
  });

  describe('错误处理测试', () => {
    it('应该能够处理解密错误数据而不抛出异常', () => {
      const invalidEncrypted = 'invalid_base64_string!';
      
      expect(() => {
        decryptData(invalidEncrypted);
      }).not.toThrow();
    });

    it('解密错误数据应该返回空字符串或无效结果', () => {
      const invalidEncrypted = 'invalid_data';
      const decrypted = decryptData(invalidEncrypted);
      
      // 具体的错误处理行为取决于实现
      expect(decrypted).toBeTruthy();
    });

    it('应该能够处理超长字符串', () => {
      const longText = 'a'.repeat(10000);
      
      const encrypted = encryptData(longText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(longText);
    });

    it('应该能够处理超长对象', () => {
      const largeObject = {};
      for (let i = 0; i < 1000; i++) {
        largeObject[`key${i}`] = `value${i}`.repeat(10);
      }
      
      const encrypted = encryptData(largeObject);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(largeObject));
    });
  });

  describe('Unicode和特殊字符测试', () => {
    it('应该能够处理emoji表情', () => {
      const originalText = 'Hello 🌍 🎉 🚀';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够处理各种语言的字符', () => {
      const originalText = '中文 English 日本語 한글 العربية Русский';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够处理换行符和制表符', () => {
      const originalText = 'line1\nline2\ttab';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够处理JSON格式的字符串', () => {
      const originalText = '{"key":"value","number":123}';
      
      const encrypted = encryptData(originalText);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(originalText);
    });
  });

  describe('综合测试场景', () => {
    it('应该支持连续加密解密操作', () => {
      const originalText = 'test';
      
      let encrypted = originalText;
      for (let i = 0; i < 5; i++) {
        encrypted = encryptData(encrypted);
      }
      
      let decrypted = encrypted;
      for (let i = 0; i < 5; i++) {
        decrypted = decryptData(decrypted);
      }
      
      expect(decrypted).toBe(originalText);
    });

    it('应该能够加密和解密复杂的数据结构', () => {
      const complexData = {
        users: [
          { id: 1, name: '张三', roles: ['admin', 'user'] },
          { id: 2, name: '李四', roles: ['user'] }
        ],
        metadata: {
          version: '1.0',
          timestamp: Date.now(),
          tags: ['production', 'stable']
        }
      };
      
      const encrypted = encryptData(complexData);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(complexData));
    });

    it('应该能够处理包含null和undefined的对象', () => {
      const objectWithNull = {
        name: 'test',
        value: null,
        undefinedField: undefined,
        nested: {
          nullValue: null
        }
      };
      
      const encrypted = encryptData(objectWithNull);
      const decrypted = decryptData(encrypted);
      
      expect(decrypted).toBe(JSON.stringify(objectWithNull));
    });
  });

  describe('性能测试', () => {
    it('应该能够快速处理大量加密操作', () => {
      const testString = 'test string';
      const iterations = 100;
      
      const startTime = Date.now();
      for (let i = 0; i < iterations; i++) {
        const encrypted = encryptData(testString);
        decryptData(encrypted);
      }
      const endTime = Date.now();
      
      // 性能测试：100次加密解密应该在合理时间内完成（< 5秒）
      expect(endTime - startTime).toBeLessThan(5000);
    });
  });
});
