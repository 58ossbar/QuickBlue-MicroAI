import { describe, it, expect, vi, beforeEach } from 'vitest';
import { defaultTimeRanges } from './default-time-ranges';
import dayjs from 'dayjs';

describe('default-time-ranges', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('基本结构测试', () => {
    it('应该是一个ref对象', () => {
      expect(defaultTimeRanges.value).toBeDefined();
      expect(Array.isArray(defaultTimeRanges.value)).toBe(true);
    });

    it('应该包含所有预定义的时间范围', () => {
      const labels = defaultTimeRanges.value.map(item => item.label);
      
      expect(labels).toContain('今日');
      expect(labels).toContain('昨日');
      expect(labels).toContain('本月');
      expect(labels).toContain('上个月');
      expect(labels).toContain('下个月');
      expect(labels).toContain('本年度');
      expect(labels).toContain('上年度');
    });

    it('每个时间范围应该有label和value属性', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(item).toHaveProperty('label');
        expect(item).toHaveProperty('value');
        expect(Array.isArray(item.value)).toBe(true);
        expect(item.value.length).toBe(2);
      });
    });
  });

  describe('今日时间范围测试', () => {
    it('今日时间范围的开始和结束应该是当前日期', () => {
      const todayRange = defaultTimeRanges.value.find(item => item.label === '今日');
      
      expect(todayRange).toBeDefined();
      expect(todayRange.value[0].isSame(dayjs(), 'day')).toBe(true);
      expect(todayRange.value[1].isSame(dayjs(), 'day')).toBe(true);
    });

    it('今日时间范围的值应该是dayjs对象', () => {
      const todayRange = defaultTimeRanges.value.find(item => item.label === '今日');
      
      expect(dayjs.isDayjs(todayRange.value[0])).toBe(true);
      expect(dayjs.isDayjs(todayRange.value[1])).toBe(true);
    });
  });

  describe('昨日时间范围测试', () => {
    it('昨日时间范围应该是昨天的一整天', () => {
      const yesterdayRange = defaultTimeRanges.value.find(item => item.label === '昨日');
      
      expect(yesterdayRange).toBeDefined();
      expect(yesterdayRange.value[0].isSame(dayjs().subtract(1, 'days'), 'day')).toBe(true);
      expect(yesterdayRange.value[1].isSame(dayjs().subtract(1, 'days'), 'day')).toBe(true);
    });

    it('昨日时间范围的开始和结束应该是同一天', () => {
      const yesterdayRange = defaultTimeRanges.value.find(item => item.label === '昨日');
      
      expect(yesterdayRange.value[0].isSame(yesterdayRange.value[1], 'day')).toBe(true);
    });
  });

  describe('本月时间范围测试', () => {
    it('本月时间范围应该从本月1号到本月最后一天', () => {
      const thisMonthRange = defaultTimeRanges.value.find(item => item.label === '本月');
      
      expect(thisMonthRange).toBeDefined();
      expect(thisMonthRange.value[0].isSame(dayjs().startOf('month'), 'day')).toBe(true);
      expect(thisMonthRange.value[1].isSame(dayjs().endOf('month'), 'day')).toBe(true);
    });

    it('本月时间范围的开始应该是1号', () => {
      const thisMonthRange = defaultTimeRanges.value.find(item => item.label === '本月');
      
      expect(thisMonthRange.value[0].date()).toBe(1);
    });
  });

  describe('上个月时间范围测试', () => {
    it('上个月时间范围应该从上个月1号到上个月最后一天', () => {
      const lastMonthRange = defaultTimeRanges.value.find(item => item.label === '上个月');
      
      expect(lastMonthRange).toBeDefined();
      const lastMonth = dayjs().subtract(1, 'months');
      expect(lastMonthRange.value[0].isSame(lastMonth.startOf('month'), 'day')).toBe(true);
      expect(lastMonthRange.value[1].isSame(lastMonth.endOf('month'), 'day')).toBe(true);
    });

    it('上个月时间范围的开始应该是1号', () => {
      const lastMonthRange = defaultTimeRanges.value.find(item => item.label === '上个月');
      
      expect(lastMonthRange.value[0].date()).toBe(1);
    });
  });

  describe('下个月时间范围测试', () => {
    it('下个月时间范围应该从下个月1号到下个月最后一天', () => {
      const nextMonthRange = defaultTimeRanges.value.find(item => item.label === '下个月');
      
      expect(nextMonthRange).toBeDefined();
      const nextMonth = dayjs().subtract(-1, 'months');
      expect(nextMonthRange.value[0].isSame(nextMonth.startOf('month'), 'day')).toBe(true);
      expect(nextMonthRange.value[1].isSame(nextMonth.endOf('month'), 'day')).toBe(true);
    });

    it('下个月时间范围的开始应该是1号', () => {
      const nextMonthRange = defaultTimeRanges.value.find(item => item.label === '下个月');
      
      expect(nextMonthRange.value[0].date()).toBe(1);
    });
  });

  describe('本年度时间范围测试', () => {
    it('本年度时间范围应该从1月1日到12月31日', () => {
      const thisYearRange = defaultTimeRanges.value.find(item => item.label === '本年度');
      
      expect(thisYearRange).toBeDefined();
      expect(thisYearRange.value[0].isSame(dayjs().startOf('year'), 'day')).toBe(true);
      expect(thisYearRange.value[1].isSame(dayjs().endOf('year'), 'day')).toBe(true);
    });

    it('本年度时间范围的开始月份应该是1月', () => {
      const thisYearRange = defaultTimeRanges.value.find(item => item.label === '本年度');
      
      expect(thisYearRange.value[0].month()).toBe(0); // dayjs中月份从0开始
      expect(thisYearRange.value[0].date()).toBe(1);
    });

    it('本年度时间范围的结束月份应该是12月', () => {
      const thisYearRange = defaultTimeRanges.value.find(item => item.label === '本年度');
      
      expect(thisYearRange.value[1].month()).toBe(11); // dayjs中12月是11
    });
  });

  describe('上年度时间范围测试', () => {
    it('上年度时间范围应该从去年1月1日到12月31日', () => {
      const lastYearRange = defaultTimeRanges.value.find(item => item.label === '上年度');
      
      expect(lastYearRange).toBeDefined();
      const lastYear = dayjs().subtract(1, 'years');
      expect(lastYearRange.value[0].isSame(lastYear.startOf('year'), 'day')).toBe(true);
      expect(lastYearRange.value[1].isSame(lastYear.endOf('year'), 'day')).toBe(true);
    });

    it('上年度的年份应该是当前年份减1', () => {
      const lastYearRange = defaultTimeRanges.value.find(item => item.label === '上年度');
      
      expect(lastYearRange.value[0].year()).toBe(dayjs().year() - 1);
      expect(lastYearRange.value[1].year()).toBe(dayjs().year() - 1);
    });
  });

  describe('时间范围顺序测试', () => {
    it('所有时间范围的开始日期应该不晚于结束日期', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(item.value[0].isBefore(item.value[1]) || item.value[0].isSame(item.value[1])).toBe(true);
      });
    });

    it('相邻时间范围应该是按时间顺序排列的', () => {
      // 今日应该在昨日之后
      const today = defaultTimeRanges.value.find(item => item.label === '今日');
      const yesterday = defaultTimeRanges.value.find(item => item.label === '昨日');
      expect(today.value[0].isAfter(yesterday.value[0])).toBe(true);
      
      // 下个月应该在本月之后
      const thisMonth = defaultTimeRanges.value.find(item => item.label === '本月');
      const nextMonth = defaultTimeRanges.value.find(item => item.label === '下个月');
      expect(nextMonth.value[0].isAfter(thisMonth.value[0])).toBe(true);
    });
  });

  describe('边界情况测试', () => {
    it('应该能够处理跨年边界的情况', () => {
      const lastMonthRange = defaultTimeRanges.value.find(item => item.label === '上个月');
      const nextMonthRange = defaultTimeRanges.value.find(item => item.label === '下个月');
      
      // 在1月份时，上个月应该是去年12月
      const january = dayjs().month(0).date(1);
      if (dayjs().month() === 0) {
        expect(lastMonthRange.value[0].year()).toBe(dayjs().year() - 1);
        expect(lastMonthRange.value[0].month()).toBe(11);
      }
    });

    it('应该能够处理闰年2月的情况', () => {
      const thisMonthRange = defaultTimeRanges.value.find(item => item.label === '本月');
      const lastMonthRange = defaultTimeRanges.value.find(item => item.label === '上个月');
      
      if (dayjs().month() === 1) { // 2月
        const daysInFebruary = dayjs().daysInMonth();
        expect(thisMonthRange.value[1].date()).toBe(daysInFebruary);
      }
    });
  });

  describe('格式和类型测试', () => {
    it('所有时间范围的label应该是字符串类型', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(typeof item.label).toBe('string');
      });
    });

    it('所有时间范围的label不应该是空的', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(item.label.length).toBeGreaterThan(0);
      });
    });

    it('所有时间范围的value应该是数组类型', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(Array.isArray(item.value)).toBe(true);
      });
    });

    it('每个时间范围的value应该包含两个元素', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(item.value.length).toBe(2);
      });
    });

    it('时间范围的value元素应该是dayjs对象', () => {
      defaultTimeRanges.value.forEach(item => {
        expect(dayjs.isDayjs(item.value[0])).toBe(true);
        expect(dayjs.isDayjs(item.value[1])).toBe(true);
      });
    });
  });

  describe('完整性和一致性测试', () => {
    it('时间范围的数量应该固定', () => {
      expect(defaultTimeRanges.value.length).toBe(7);
    });

    it('时间范围的label应该是唯一的', () => {
      const labels = defaultTimeRanges.value.map(item => item.label);
      const uniqueLabels = [...new Set(labels)];
      expect(labels.length).toBe(uniqueLabels.length);
    });

    it('时间范围应该覆盖常见的时间选择需求', () => {
      const labels = defaultTimeRanges.value.map(item => item.label);
      
      // 应该包含日、月、年的快捷选择
      expect(labels.some(l => l.includes('日'))).toBe(true);
      expect(labels.some(l => l.includes('月'))).toBe(true);
      expect(labels.some(l => l.includes('年'))).toBe(true);
    });
  });
});
