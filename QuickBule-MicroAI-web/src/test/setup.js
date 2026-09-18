import { vi } from 'vitest';

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  clear: vi.fn(),
  removeItem: vi.fn(),
};

global.localStorage = localStorageMock;

// Mock window.location
delete global.window.location;
global.window.location = {
  href: '/',
  assign: vi.fn(),
  reload: vi.fn(),
};

// Reset mocks before each test
beforeEach(() => {
  localStorageMock.getItem.mockClear();
  localStorageMock.setItem.mockClear();
  localStorageMock.clear.mockClear();
  localStorageMock.removeItem.mockClear();
});
