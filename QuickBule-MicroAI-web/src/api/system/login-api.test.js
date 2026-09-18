import { describe, it, expect, vi } from 'vitest';
import { getRequest, postRequest } from '/@/lib/axios';
import { loginApi } from './login-api';

// Mock axios 请求方法
vi.mock('/@/lib/axios', () => ({
  getRequest: vi.fn(),
  postRequest: vi.fn(),
}));

describe('login-api.js', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('应该调用 postRequest 发送登录请求', async () => {
      const loginData = {
        loginName: 'test',
        password: 'password',
      };
      const mockResponse = { code: 0, data: { token: 'test-token' } };
      postRequest.mockResolvedValue(mockResponse);
      
      const result = await loginApi.login(loginData);
      
      expect(postRequest).toHaveBeenCalledWith('/login', loginData);
      expect(result).toEqual(mockResponse);
    });

    it('应该传递正确的 URL', async () => {
      const loginData = { loginName: 'test', password: 'password' };
      postRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.login(loginData);
      
      expect(postRequest).toHaveBeenCalledWith('/login', loginData);
    });

    it('应该支持不同参数格式', async () => {
      const loginData = {
        loginName: 'admin',
        password: '123456',
        captcha: 'abc123',
      };
      postRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.login(loginData);
      
      expect(postRequest).toHaveBeenCalledWith('/login', loginData);
    });
  });

  describe('logout', () => {
    it('应该调用 getRequest 发送退出登录请求', async () => {
      const mockResponse = { code: 0 };
      getRequest.mockResolvedValue(mockResponse);
      
      const result = await loginApi.logout();
      
      expect(getRequest).toHaveBeenCalledWith('/login/logout');
      expect(result).toEqual(mockResponse);
    });

    it('应该传递正确的退出 URL', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.logout();
      
      expect(getRequest).toHaveBeenCalledWith('/login/logout');
    });

    it('不需要传递参数', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.logout();
      
      expect(getRequest).toHaveBeenCalledWith('/login/logout');
    });
  });

  describe('getCaptcha', () => {
    it('应该调用 getRequest 获取验证码', async () => {
      const mockResponse = { code: 0, data: 'captcha-image' };
      getRequest.mockResolvedValue(mockResponse);
      
      const result = await loginApi.getCaptcha();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getCaptcha');
      expect(result).toEqual(mockResponse);
    });

    it('应该传递正确的验证码 URL', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.getCaptcha();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getCaptcha');
    });
  });

  describe('getLoginInfo', () => {
    it('应该调用 getRequest 获取登录信息', async () => {
      const mockResponse = {
        code: 0,
        data: {
          userId: 1,
          userName: 'test',
          menuList: [],
        },
      };
      getRequest.mockResolvedValue(mockResponse);
      
      const result = await loginApi.getLoginInfo();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getLoginInfo');
      expect(result).toEqual(mockResponse);
    });

    it('应该传递正确的登录信息 URL', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.getLoginInfo();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getLoginInfo');
    });

    it('不需要传递参数', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.getLoginInfo();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getLoginInfo');
    });
  });

  describe('sendLoginEmailCode', () => {
    it('应该调用 getRequest 发送邮箱验证码', async () => {
      const loginName = 'test@example.com';
      const mockResponse = { code: 0 };
      getRequest.mockResolvedValue(mockResponse);
      
      const result = await loginApi.sendLoginEmailCode(loginName);
      
      expect(getRequest).toHaveBeenCalledWith(`/login/sendEmailCode/${loginName}`);
      expect(result).toEqual(mockResponse);
    });

    it('应该正确拼接 URL 参数', async () => {
      const loginName = 'user@test.com';
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.sendLoginEmailCode(loginName);
      
      expect(getRequest).toHaveBeenCalledWith('/login/sendEmailCode/user@test.com');
    });

    it('应该支持不同的登录名格式', async () => {
      const loginNames = ['test@example.com', 'user@test.cn', 'admin@test.org'];
      getRequest.mockResolvedValue({ code: 0 });
      
      for (const loginName of loginNames) {
        await loginApi.sendLoginEmailCode(loginName);
        expect(getRequest).toHaveBeenCalledWith(`/login/sendEmailCode/${loginName}`);
      }
    });
  });

  describe('getTwoFactorLoginFlag', () => {
    it('应该调用 getRequest 获取双因子登录标识', async () => {
      const mockResponse = { code: 0, data: true };
      getRequest.mockResolvedValue(mockResponse);
      
      const result = await loginApi.getTwoFactorLoginFlag();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getTwoFactorLoginFlag');
      expect(result).toEqual(mockResponse);
    });

    it('应该传递正确的双因子登录 URL', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.getTwoFactorLoginFlag();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getTwoFactorLoginFlag');
    });

    it('不需要传递参数', async () => {
      getRequest.mockResolvedValue({ code: 0 });
      
      await loginApi.getTwoFactorLoginFlag();
      
      expect(getRequest).toHaveBeenCalledWith('/login/getTwoFactorLoginFlag');
    });
  });

  describe('集成测试', () => {
    it('完整的登录流程调用', async () => {
      // 1. 获取验证码
      getRequest.mockResolvedValue({ code: 0, data: 'captcha' });
      await loginApi.getCaptcha();
      expect(getRequest).toHaveBeenCalledWith('/login/getCaptcha');
      
      // 2. 登录
      postRequest.mockResolvedValue({ code: 0, data: { token: 'test-token' } });
      await loginApi.login({ loginName: 'test', password: 'password' });
      expect(postRequest).toHaveBeenCalledWith('/login', { loginName: 'test', password: 'password' });
      
      // 3. 获取登录信息
      getRequest.mockResolvedValue({ code: 0, data: { userId: 1 } });
      await loginApi.getLoginInfo();
      expect(getRequest).toHaveBeenCalledWith('/login/getLoginInfo');
    });

    it('完整的退出流程调用', async () => {
      // 退出登录
      getRequest.mockResolvedValue({ code: 0 });
      await loginApi.logout();
      expect(getRequest).toHaveBeenCalledWith('/login/logout');
    });

    it('邮箱验证码登录流程', async () => {
      // 发送邮箱验证码
      getRequest.mockResolvedValue({ code: 0 });
      await loginApi.sendLoginEmailCode('test@example.com');
      expect(getRequest).toHaveBeenCalledWith('/login/sendEmailCode/test@example.com');
      
      // 使用验证码登录
      postRequest.mockResolvedValue({ code: 0, data: { token: 'test-token' } });
      await loginApi.login({ loginName: 'test@example.com', emailCode: '123456' });
      expect(postRequest).toHaveBeenCalledWith('/login', { loginName: 'test@example.com', emailCode: '123456' });
    });
  });
});
