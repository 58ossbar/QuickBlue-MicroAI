package com.budaos.support.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数字安全工具类
 *
 * @author budaos
 */
public class DigitalSecureUtil {

    private static final long[] PRIME_FACTORS = {999983L, 999979L, 999961L, 999959L, 999953L};
    private static final long MIXING_CONSTANT = 6364136223846793005L;

    // 用于记录已生成的数字（内存缓存，短期防重复）
    private static final ConcurrentHashMap<Integer, Set<Long>> generatedCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 10000;

    /**
     * 根据安全模式生成安全数字（确保唯一）
     */
    public static synchronized long generateSecureNumber(Long originalSequence, Integer serialNumberId,
                                                         Integer secureMode, Integer randomRange, String encryptKey,
                                                         Set<Long> existingNumbers) {
        if (secureMode == null || secureMode == 0) {
            return originalSequence; // 普通模式，直接返回
        }

        long candidate;
        int maxRetry = 10; // 最大重试次数

        for (int retry = 0; retry < maxRetry; retry++) {
            switch (secureMode) {
                case 1: // 随机模式 - 增强版
                    candidate = generateEnhancedRandomNumber(originalSequence, randomRange, serialNumberId, retry);
                    break;
                case 2: // 时间戳模式
                    candidate = generateTimestampNumber(originalSequence, serialNumberId, retry);
                    break;
                case 3: // 加密模式
                    candidate = generateEncryptedNumber(originalSequence, serialNumberId, encryptKey, retry);
                    break;
                default:
                    candidate = originalSequence;
            }

            // 检查唯一性
            if (isUnique(candidate, serialNumberId, existingNumbers)) {
                cacheGeneratedNumber(serialNumberId, candidate);
                return candidate;
            }

            // 如果不唯一，增加一些扰动再试
            originalSequence = originalSequence + 1 + retry;
        }

        // 如果重试多次仍然冲突，使用保底方案：时间戳+序列号
        return generateFallbackUniqueNumber(originalSequence, serialNumberId);
    }

    /**
     * 检查数字是否唯一
     */
    private static boolean isUnique(long number, Integer serialNumberId, Set<Long> existingNumbers) {
        // 检查内存缓存
        Set<Long> cache = generatedCache.get(serialNumberId);
        if (cache != null && cache.contains(number)) {
            return false;
        }

        // 检查传入的已有数字集合
        if (existingNumbers != null && existingNumbers.contains(number)) {
            return false;
        }

        return true;
    }

    /**
     * 缓存已生成的数字
     */
    private static void cacheGeneratedNumber(Integer serialNumberId, long number) {
        Set<Long> cache = generatedCache.computeIfAbsent(serialNumberId, k -> new HashSet<>());

        // 限制缓存大小
        if (cache.size() >= MAX_CACHE_SIZE) {
            cache.clear();
        }

        cache.add(number);
    }

    /**
     * 清除缓存
     */
    public static void clearCache(Integer serialNumberId) {
        if (serialNumberId == null) {
            generatedCache.clear();
        } else {
            generatedCache.remove(serialNumberId);
        }
    }

    /**
     * 生成增强随机数字（确保唯一性）
     */
    private static long generateEnhancedRandomNumber(Long sequence, Integer range, Integer salt, int retryCount) {
        if (range == null || range <= 1) {
            range = 1000; // 增大默认范围
        }

        // 使用更强的混合算法，加入重试计数作为扰动
        long mixed = (sequence * MIXING_CONSTANT + salt * (retryCount + 1)) % 100000000000L;

        // 确保数字不会太小（至少10位）
        if (mixed < 10000000000L) {
            mixed += 10000000000L;
        }

        // 随机跳跃（增加随机性）
        long jump = ThreadLocalRandom.current().nextLong(range * 2) - range;
        long result = mixed + jump;

        // 确保是11位数字
        if (result < 10000000000L) {
            result += 10000000000L;
        } else if (result >= 100000000000L) {
            result %= 100000000000L;
            if (result < 10000000000L) {
                result += 10000000000L;
            }
        }

        // 处理可能产生的连续0
        return avoidConsecutiveZeros(result);
    }

    /**
     * 生成时间戳混合数字（确保唯一性）
     */
    private static long generateTimestampNumber(Long sequence, Integer salt, int retryCount) {
        long timestamp = System.currentTimeMillis(); // 毫秒级时间戳，增加唯一性

        // 加入纳秒部分（进一步提升唯一性）
        long nanoTime = System.nanoTime() % 1000000;

        // 混合算法
        long mixed = (sequence * PRIME_FACTORS[salt % PRIME_FACTORS.length] +
                timestamp % 1000000 + nanoTime + retryCount) % 100000000000L;

        // 确保是11位正整数
        long result = Math.abs(mixed) % 100000000000L;

        if (result < 10000000000L) {
            result += 10000000000L;
        }

        return avoidConsecutiveZeros(result);
    }

    /**
     * 生成加密数字
     */
    private static long generateEncryptedNumber(Long sequence, Integer salt, String key, int retryCount) {
        // Feistel网络结构
        long left = sequence / 100000L;
        long right = sequence % 100000L;

        // 使用盐值作为轮密钥
        long roundKey = StringUtils.isNotEmpty(key) ?
                Math.abs(key.hashCode()) : (salt * 123456789L);

        // 加入重试计数增加变化
        roundKey += retryCount;

        // 3轮变换
        for (int i = 0; i < 3; i++) {
            long temp = right;
            right = left ^ feistelFunction(right, roundKey + i);
            left = temp;
        }

        // 重新组合为11位
        long result = Math.abs(left * 100000L + right) % 100000000000L;

        if (result < 10000000000L) {
            result += 10000000000L;
        }

        return avoidConsecutiveZeros(result);
    }

    /**
     * 生成保底唯一编号（基于时间戳）
     */
    private static long generateFallbackUniqueNumber(Long sequence, Integer salt) {
        // 使用高精度时间戳确保唯一
        long timestamp = System.currentTimeMillis();
        long nanoPart = System.nanoTime() % 10000;

        // 组合：时间戳后9位 + 序列号后2位 + 纳秒后2位
        long timePart = timestamp % 1000000000L; // 9位
        long seqPart = sequence % 100L; // 2位
        long nanoPart2 = nanoPart % 100L; // 2位

        long result = timePart * 10000L + seqPart * 100L + nanoPart2;

        // 确保是11位
        if (result < 10000000000L) {
            result += 10000000000L;
        } else if (result >= 100000000000L) {
            result %= 100000000000L;
            if (result < 10000000000L) {
                result += 10000000000L;
            }
        }

        return avoidConsecutiveZeros(result);
    }

    /**
     * 批量生成唯一数字
     */
    public static List<Long> generateBatchUniqueNumbers(Long startSequence, Integer serialNumberId,
                                                        Integer secureMode, Integer randomRange,
                                                        String encryptKey, int count) {
        List<Long> result = new ArrayList<>(count);
        Set<Long> tempSet = new HashSet<>(count * 2);

        long currentSeq = startSequence;

        for (int i = 0; i < count; i++) {
            long uniqueNumber;
            int retry = 0;

            do {
                uniqueNumber = generateSecureNumber(
                        currentSeq + i + retry,
                        serialNumberId,
                        secureMode,
                        randomRange,
                        encryptKey,
                        tempSet
                );
                retry++;

                // 安全限制，避免无限循环
                if (retry > 100) {
                    uniqueNumber = generateFallbackUniqueNumber(currentSeq + i, serialNumberId);
                    break;
                }
            } while (tempSet.contains(uniqueNumber));

            tempSet.add(uniqueNumber);
            result.add(uniqueNumber);
        }

        return result;
    }

    /**
     * 避免连续0
     */
    private static long avoidConsecutiveZeros(long number) {
        String numStr = String.valueOf(number);
        if (!hasConsecutiveZeros(numStr, 3)) {
            return number;
        }

        char[] chars = numStr.toCharArray();

        // 检查是否有连续3个或更多0
        for (int i = 0; i < chars.length - 2; i++) {
            if (chars[i] == '0' && chars[i + 1] == '0' && chars[i + 2] == '0') {
                // 替换中间的0为非0数字
                int replaceIndex = i + 1;
                int newDigit;
                do {
                    newDigit = ThreadLocalRandom.current().nextInt(1, 10);
                } while (newDigit == chars[replaceIndex - 1] - '0' ||
                        (replaceIndex + 1 < chars.length && newDigit == chars[replaceIndex + 1] - '0'));

                chars[replaceIndex] = (char) (newDigit + '0');
                break;
            }
        }

        // 检查是否有连续2个0（可选，增加随机性）
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '0' && chars[i + 1] == '0' && i > 0 && i < chars.length - 2) {
                // 50%概率替换其中一个0
                if (ThreadLocalRandom.current().nextBoolean()) {
                    int replaceIndex = i + 1;
                    int newDigit;
                    do {
                        newDigit = ThreadLocalRandom.current().nextInt(1, 10);
                    } while (newDigit == chars[replaceIndex - 1] - '0');

                    chars[replaceIndex] = (char) (newDigit + '0');
                }
            }
        }

        return Long.parseLong(new String(chars));
    }

    private static boolean hasConsecutiveZeros(String str, int count) {
        String zeros = "0".repeat(count);
        return str.contains(zeros);
    }

    /**
     * 计算校验位（Luhn算法）
     */
    public static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n = n - 9;
            }
            sum += n;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    /**
     * 生成校验码（优化版，控制数字长度）
     */
    public static String appendCheckDigitOptimized(long number, int maxLength) {
        String numStr = String.valueOf(Math.abs(number));

        // 如果数字太长，取后(maxLength-1)位，留1位给校验位
        if (numStr.length() > maxLength - 1) {
            numStr = numStr.substring(numStr.length() - (maxLength - 1));
        }

        // 确保数字足够大，避免前导0
        long numValue = Long.parseLong(numStr);
        if (numValue < Math.pow(10, maxLength - 3)) {
            // 添加一些随机性
            long enhanced = numValue + ThreadLocalRandom.current().nextInt(1000, 10000);
            numStr = String.valueOf(enhanced);

            // 再次检查长度
            if (numStr.length() > maxLength - 1) {
                numStr = numStr.substring(numStr.length() - (maxLength - 1));
            }
        }

        int checkDigit = calculateLuhnCheckDigit(numStr);
        return numStr + checkDigit;
    }

    /**
     * 生成高质量随机数字（特别针对11位格式）
     */
    public static long generateHighQualityRandom(Long base, Integer salt) {
        // 使用多个随机源混合
        long random1 = ThreadLocalRandom.current().nextLong(100000, 1000000);
        long random2 = ThreadLocalRandom.current().nextLong(10000, 100000);
        long random3 = ThreadLocalRandom.current().nextLong(1000, 10000);

        // 混合算法
        long mixed = (base * 48271L + salt * 69621L + random1 * 314159L) % 10000000000L;

        // 确保是11位
        long result = (mixed * 100 + random2 % 100) * 1000 + random3 % 1000;

        // 再次混合
        result = (result ^ (salt * 123456789L)) % 100000000000L;

        // 确保在有效范围内
        if (result < 10000000000L) {
            result += 10000000000L;
        } else if (result >= 100000000000L) {
            result %= 100000000000L;
        }

        // 避免连续0
        return avoidMultipleZeros(result);
    }

    /**
     * 避免多个0
     */
    private static long avoidMultipleZeros(long number) {
        String numStr = String.valueOf(number);
        if (!hasConsecutiveZeros(numStr, 3)) {
            return number;
        }

        char[] chars = numStr.toCharArray();
        for (int i = 0; i < chars.length - 2; i++) {
            if (chars[i] == '0' && chars[i + 1] == '0') {
                // 找到连续0，替换其中一个
                int replaceIndex = i + 1;
                int newDigit = ThreadLocalRandom.current().nextInt(1, 10);
                chars[replaceIndex] = (char) (newDigit + '0');
            }
        }

        return Long.parseLong(new String(chars));
    }

    private static long feistelFunction(long input, long key) {
        return (input * key) % 100000L;
    }

    /**
     * 获取已生成数字的数量（用于监控）
     */
    public static int getCacheSize(Integer serialNumberId) {
        Set<Long> cache = generatedCache.get(serialNumberId);
        return cache != null ? cache.size() : 0;
    }

    /**
     * 生成简单的随机数字（不保证唯一性，用于测试）
     */
    public static long generateSimpleRandom() {
        long result = ThreadLocalRandom.current().nextLong(10000000000L, 100000000000L);
        return avoidConsecutiveZeros(result);
    }
}
