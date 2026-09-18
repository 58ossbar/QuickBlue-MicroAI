package com.budaos.support.service.impl;

import com.budaos.common.core.exception.BizException;
import com.budaos.support.service.SerialCodeService;
import com.budaos.common.core.util.EnumValueUtil;
import com.budaos.support.constant.SerialCodeIdEnum;
import com.budaos.support.constant.SerialCodeRuleTypeEnum;
import com.budaos.support.constant.SerialCodeSecureModeEnum;
import com.budaos.support.dao.SerialCodeDao;
import com.budaos.support.dao.SerialCodeRecordDao;
import com.budaos.support.domain.entity.SerialCodeEntity;
import com.budaos.support.domain.entity.SerialCodeRecordEntity;
import com.budaos.support.domain.bo.SerialCodeGenerateResultBO;
import com.budaos.support.domain.bo.SerialCodeInfoBO;
import com.budaos.support.domain.bo.SerialCodeLastGenerateBO;
import com.budaos.support.util.DigitalSecureUtil;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static com.budaos.support.domain.entity.SerialCodeRecordEntity.*;

/**
 * 单据序列�?基类
 *
 */
@Slf4j
public abstract class SerialCodeBaseService implements SerialCodeService {

    @Resource
    protected SerialCodeRecordDao serialNumberRecordDao;

    @Resource
    protected SerialCodeDao serialNumberDao;

    protected ConcurrentHashMap<Integer, SerialCodeInfoBO> serialNumberMap = new ConcurrentHashMap<>();

    public abstract List<String> generateSerialNumberList(SerialCodeInfoBO serialNumber, int count);

    @PostConstruct
    void init() {
        List<SerialCodeEntity> serialNumberEntityList = serialNumberDao.selectList(null);
        if (serialNumberEntityList == null) {
            return;
        }
        for (SerialCodeEntity serialNumberEntity : serialNumberEntityList) {
            SerialCodeRuleTypeEnum ruleTypeEnum = null;
            String ruleType = serialNumberEntity.getRuleType().toUpperCase();
            for (SerialCodeRuleTypeEnum type : SerialCodeRuleTypeEnum.values()) {
                if (type.name().equals(ruleType)) {
                    ruleTypeEnum = type;
                    break;
                }
            }
            if (ruleTypeEnum == null) {
                throw new ExceptionInInitializerError("cannot find rule type , id : " + serialNumberEntity.getSerialNumberId());
            }

            String format = serialNumberEntity.getFormat();

            // 解析格式，提取各部分信息
            SerialNumberFormatInfo formatInfo = parseFormat(format, serialNumberEntity.getSerialNumberId());

            if (serialNumberEntity.getStepRandomRange() < 1) {
                throw new ExceptionInInitializerError("random step range must greater than 1 " + serialNumberEntity.getSerialNumberId());
            }

            // 获取安全模式枚举
            SerialCodeSecureModeEnum secureModeEnum = EnumValueUtil.getEnumByValue(
                    serialNumberEntity.getSecureMode(), SerialCodeSecureModeEnum.class);
            if (secureModeEnum == null) {
                secureModeEnum = SerialCodeSecureModeEnum.NONE;
            }

            SerialCodeInfoBO serialNumberInfoBO = SerialCodeInfoBO.builder()
                    .serialNumberId(serialNumberEntity.getSerialNumberId())
                    .serialNumberRuleTypeEnum(ruleTypeEnum)
                    .serialNumberSecureModeEnum(secureModeEnum)
                    .initNumber(serialNumberEntity.getInitNumber())
                    .format(serialNumberEntity.getFormat())
                    .stepRandomRange(serialNumberEntity.getStepRandomRange())
                    .secureMode(serialNumberEntity.getSecureMode())
                    .randomRange(serialNumberEntity.getRandomRange())
                    .encryptKey(serialNumberEntity.getEncryptKey())
                    .haveYearFlag(formatInfo.haveYearFlag)
                    .haveMonthFlag(formatInfo.haveMonthFlag)
                    .haveDayFlag(formatInfo.haveDayFlag)
                    .numberCount(formatInfo.numberCount)
                    .numberFormat(formatInfo.numberFormat)
                    .prefix(formatInfo.prefix)
                    .suffix(formatInfo.suffix)
                    .yearFormat(formatInfo.yearFormat)
                    .monthFormat(formatInfo.monthFormat)
                    .dayFormat(formatInfo.dayFormat)
                    .yearRegex(formatInfo.yearRegex)
                    .monthRegex(formatInfo.monthRegex)
                    .dayRegex(formatInfo.dayRegex)
                    .build();

            this.serialNumberMap.put(serialNumberEntity.getSerialNumberId(), serialNumberInfoBO);
        }

        //初始化数�?
        initLastGenerateData(serialNumberEntityList);
    }

    /**
     * 解析格式字符�?
     */
    private SerialNumberFormatInfo parseFormat(String format, Integer serialNumberId) {
        SerialNumberFormatInfo info = new SerialNumberFormatInfo();

        // 提取数字占位�?[n...]
        Pattern numberPattern = Pattern.compile("\\[(n+)\\]");
        Matcher numberMatcher = numberPattern.matcher(format);

        if (numberMatcher.find()) {
            String nPart = numberMatcher.group(1);
            info.numberCount = nPart.length();
            info.numberFormat = "\\[" + nPart + "\\]";
            info.numberPlaceholder = "[" + nPart + "]";
        } else {
            throw new ExceptionInInitializerError("未找到数字占位符[n...]，请使用标准格式，id: " + serialNumberId);
        }

        // 检查年月日占位�?
        info.haveYearFlag = format.contains(SerialCodeRuleTypeEnum.YEAR.getValue());
        info.haveMonthFlag = format.contains(SerialCodeRuleTypeEnum.MONTH.getValue());
        info.haveDayFlag = format.contains(SerialCodeRuleTypeEnum.DAY.getValue());

        // 设置年月日的正则表达�?
        info.yearFormat = SerialCodeRuleTypeEnum.YEAR.getValue();
        info.monthFormat = SerialCodeRuleTypeEnum.MONTH.getValue();
        info.dayFormat = SerialCodeRuleTypeEnum.DAY.getValue();
        info.yearRegex = SerialCodeRuleTypeEnum.YEAR.getRegex();
        info.monthRegex = SerialCodeRuleTypeEnum.MONTH.getRegex();
        info.dayRegex = SerialCodeRuleTypeEnum.DAY.getRegex();

        // 关键修正：正确提取前缀和后缀
        // 先移除所有占位符，剩下的就是固定文本
        String rawFormat = format;

        // 临时移除所有占位符，用特殊标记代替
        String tempFormat = rawFormat
                .replace(info.yearFormat, "{Y}")
                .replace(info.monthFormat, "{M}")
                .replace(info.dayFormat, "{D}")
                .replace(info.numberPlaceholder, "{N}");

        return info;
    }

    /**
     * 格式信息内部�?
     */
    private static class SerialNumberFormatInfo {
        boolean haveYearFlag;
        boolean haveMonthFlag;
        boolean haveDayFlag;
        Integer numberCount;
        String numberFormat;
        String numberPlaceholder;
        String prefix;
        String suffix;
        String yearFormat;
        String monthFormat;
        String dayFormat;
        String yearRegex;
        String monthRegex;
        String dayRegex;
    }

    /**
     * 初始化上次生成的数据
     *
     * @param serialNumberEntityList
     */
    public abstract void initLastGenerateData(List<SerialCodeEntity> serialNumberEntityList);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generate(SerialCodeIdEnum serialNumberIdEnum) {
        List<String> generateList = this.generate(serialNumberIdEnum, 1);
        if (generateList == null || generateList.isEmpty()) {
            throw new BizException("cannot generate : " + serialNumberIdEnum.toString());
        }
        return generateList.get(0);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> generate(SerialCodeIdEnum serialNumberIdEnum, int count) {
        SerialCodeInfoBO serialNumberInfoBO = serialNumberMap.get(serialNumberIdEnum.getSerialNumberId());
        if (serialNumberInfoBO == null) {
            throw new BizException("cannot found SerialNumberId : " + serialNumberIdEnum.toString());
        }
        return this.generateSerialNumberList(serialNumberInfoBO, count);
    }

    /**
     * 循环生成 number 集合（通用版）
     */
    protected SerialCodeGenerateResultBO loopNumberList(SerialCodeLastGenerateBO lastGenerate,
                                                          SerialCodeInfoBO serialNumberInfo, int count) {
        Long lastNumber = lastGenerate.getLastNumber();
        boolean isReset = false;

        // 检查是否需要重置（基于周期规则�?
        if (isResetInitNumber(lastGenerate, serialNumberInfo)) {
            lastNumber = serialNumberInfo.getInitNumber();
            isReset = true;
        }

        ArrayList<Long> originalSequenceList = Lists.newArrayListWithCapacity(count);
        ArrayList<Long> secureNumberList = Lists.newArrayListWithCapacity(count);

        // 根据安全模式选择生成策略
        if (serialNumberInfo.getSecureMode() != null && serialNumberInfo.getSecureMode() > 0) {
            // 安全模式：生成随�?加密数字
            secureNumberList = (ArrayList<Long>) generateSecureNumbers(lastNumber, serialNumberInfo, count);

            // 更新原始序列号（连续递增�?
            for (int i = 0; i < count; i++) {
                originalSequenceList.add(lastNumber + i + 1);
            }

            // 更新最后使用的序列�?
            lastNumber = lastNumber + count;
        } else {
            // 普通模式：生成连续数字
            for (int i = 0; i < count; i++) {
                Integer stepRandomRange = serialNumberInfo.getStepRandomRange();
                if (stepRandomRange > 1) {
                    lastNumber = lastNumber + RandomUtils.nextInt(1, stepRandomRange + 1);
                } else {
                    lastNumber = lastNumber + 1;
                }

                originalSequenceList.add(lastNumber);
                secureNumberList.add(lastNumber);
            }
        }

        return SerialCodeGenerateResultBO
                .builder()
                .serialNumberId(serialNumberInfo.getSerialNumberId())
                .lastNumber(lastNumber)
                .lastTime(LocalDateTime.now())
                .numberList(secureNumberList)
                .originalSequenceList(originalSequenceList)
                .isReset(isReset)
                .build();
    }

    /**
     * 生成安全数字
     */
    private List<Long> generateSecureNumbers(Long startSequence, SerialCodeInfoBO serialNumberInfo, int count) {
        // 根据数字位数确定最小值和范围
        int numberCount = serialNumberInfo.getNumberCount();
        long minValue = (long) Math.pow(10, numberCount - 1);
        long maxValue = (long) Math.pow(10, numberCount) - 1;

        // 如果最小值为0（当numberCount=1时），设�?
        if (minValue == 0) {
            minValue = 1;
        }

        List<Long> result = new ArrayList<>(count);

        // 根据不同的安全模式生成数�?
        for (int i = 0; i < count; i++) {
            long baseNumber = startSequence + i;
            long secureNumber;

            switch (serialNumberInfo.getSecureMode()) {
                case 1: // 随机模式
                    secureNumber = generateRandomNumber(baseNumber, serialNumberInfo, minValue, maxValue);
                    break;
                case 2: // 时间戳模�?
                    secureNumber = generateTimestampNumber(baseNumber, serialNumberInfo, minValue, maxValue);
                    break;
                case 3: // 加密模式
                    secureNumber = generateEncryptedNumber(baseNumber, serialNumberInfo, minValue, maxValue);
                    break;
                default:
                    secureNumber = baseNumber;
            }

            // 确保数字在有效范围内
            secureNumber = ensureInRange(secureNumber, minValue, maxValue);

            // 避免连续0
            secureNumber = avoidConsecutiveZeros(secureNumber);

            result.add(secureNumber);
        }

        return result;
    }

    /**
     * 生成随机数字
     */
    private long generateRandomNumber(long base, SerialCodeInfoBO info, long minValue, long maxValue) {
        Random random = new Random(base + info.getSerialNumberId());

        // 基础随机�?
        long randomNum = random.nextLong() % (maxValue - minValue + 1);
        if (randomNum < 0) randomNum = -randomNum;

        // 加上最小�?
        long result = minValue + randomNum;

        // 确保不超过最大�?
        if (result > maxValue) {
            result = maxValue - (result % 1000);
        }

        return result;
    }

    /**
     * 生成时间戳数�?
     */
    private long generateTimestampNumber(long base, SerialCodeInfoBO info, long minValue, long maxValue) {
        long timestamp = System.currentTimeMillis();
        long nanoPart = System.nanoTime() % 10000;

        // 混合算法
        long mixed = (base * 48271L + info.getSerialNumberId() * 69621L + timestamp % 1000000L) % maxValue;

        // 确保不小于最小�?
        if (mixed < minValue) {
            mixed = minValue + (mixed % (maxValue - minValue + 1));
        }

        // 加入纳秒部分增加随机�?
        mixed = (mixed * 10000 + nanoPart) % maxValue;

        return mixed;
    }

    /**
     * 生成加密数字
     */
    private long generateEncryptedNumber(long base, SerialCodeInfoBO info, long minValue, long maxValue) {
        // 简单的加密算法
        long key = info.getEncryptKey() != null ? info.getEncryptKey().hashCode() : 123456789L;

        // Feistel网络结构
        long left = base / 1000L;
        long right = base % 1000L;

        // 2轮变�?
        for (int i = 0; i < 2; i++) {
            long temp = right;
            right = left ^ feistelFunction(right, key + i);
            left = temp;
        }

        long result = Math.abs(left * 1000L + right) % maxValue;

        if (result < minValue) {
            result = minValue + (result % (maxValue - minValue + 1));
        }

        return result;
    }

    private long feistelFunction(long input, long key) {
        return (input * key) % 1000L;
    }

    /**
     * 确保数字在范围内
     */
    private long ensureInRange(long number, long minValue, long maxValue) {
        if (number < minValue) {
            // 如果数字太小，加上偏移量
            long offset = minValue - number;
            number = minValue + (offset % (maxValue - minValue + 1));
        } else if (number > maxValue) {
            // 如果数字太大，取�?
            number = minValue + (number % (maxValue - minValue + 1));
        }
        return number;
    }

    /**
     * 避免连续0
     */
    private long avoidConsecutiveZeros(long number) {
        String numStr = String.valueOf(number);

        // 检查是否有连续3�?
        if (numStr.contains("000")) {
            char[] chars = numStr.toCharArray();
            Random random = new Random(number);

            for (int i = 0; i < chars.length - 2; i++) {
                if (chars[i] == '0' && chars[i + 1] == '0' && chars[i + 2] == '0') {
                    // 替换中间�?
                    int replaceIndex = i + 1;
                    int newDigit;
                    do {
                        newDigit = random.nextInt(1, 10);
                    } while (newDigit == chars[replaceIndex - 1] - '0');

                    chars[replaceIndex] = (char) (newDigit + '0');
                    break;
                }
            }

            return Long.parseLong(new String(chars));
        }

        return number;
    }
    /**
     * 格式化安全数字（优化版，直接使用完整数字�?
     */
    private String formatSecureNumberOptimized(Long number, SerialCodeInfoBO serialNumberInfo) {
        if (number == null) {
            return "0".repeat(serialNumberInfo.getNumberCount());
        }

        String numStr = String.valueOf(Math.abs(number));

        // 安全模式处理
        if (serialNumberInfo.getSecureMode() != null && serialNumberInfo.getSecureMode() > 0) {
            // 添加校验�?
            String withCheckDigit = DigitalSecureUtil.appendCheckDigitOptimized(number, serialNumberInfo.getNumberCount());
            return withCheckDigit;
        }

        // 普通模式：直接返回数字
        // 如果数字长度不够，补0（但根据新需求，应该避免这种情况�?
        if (numStr.length() < serialNumberInfo.getNumberCount()) {
            // 只有数字非常小时才补0
            if (number < 1000) {
                return String.format("%0" + serialNumberInfo.getNumberCount() + "d", number);
            }
        } else if (numStr.length() > serialNumberInfo.getNumberCount()) {
            // 如果数字太长，取后几�?
            return numStr.substring(numStr.length() - serialNumberInfo.getNumberCount());
        }

        return numStr;
    }
    protected void saveRecord(SerialCodeGenerateResultBO resultBO) {
        // 保存原始序列�?
        Long originalSequence = null;
        if (resultBO.getOriginalSequenceList() != null && !resultBO.getOriginalSequenceList().isEmpty()) {
            // 取最后一个原始序列号
            originalSequence = resultBO.getOriginalSequenceList().get(resultBO.getOriginalSequenceList().size() - 1);
        }

        // 先尝试调用增强版的方�?
        Long effectRows = null;
        try {
            // 使用反射检查方法是否存在，避免直接调用报错
            java.lang.reflect.Method method = serialNumberRecordDao.getClass().getMethod(
                    "updateRecordWithOriginal",
                    Integer.class, LocalDate.class, Long.class, Long.class, Integer.class
            );

            if (method != null) {
                effectRows = serialNumberRecordDao.updateRecordWithOriginal(
                        resultBO.getSerialNumberId(),
                        resultBO.getLastTime().toLocalDate(),
                        resultBO.getLastNumber(),
                        originalSequence,
                        resultBO.getNumberList().size()
                );
            } else {
                // 如果方法不存在，使用旧版方法
                effectRows = serialNumberRecordDao.updateRecord(
                        resultBO.getSerialNumberId(),
                        resultBO.getLastTime().toLocalDate(),
                        resultBO.getLastNumber(),
                        resultBO.getNumberList().size()
                );
            }
        } catch (NoSuchMethodException e) {
            // 方法不存在，使用旧版方法
            effectRows = serialNumberRecordDao.updateRecord(
                    resultBO.getSerialNumberId(),
                    resultBO.getLastTime().toLocalDate(),
                    resultBO.getLastNumber(),
                    resultBO.getNumberList().size()
            );
        } catch (Exception e) {
            log.error("Failed to save record", e);
            // 出现异常时尝试插入新记录
            effectRows = 0L;
        }

        // 需要插入新记录
        if (effectRows == null || effectRows == 0) {
            SerialCodeRecordEntity recordEntity = SerialCodeRecordEntity.builder()
                    .serialNumberId(resultBO.getSerialNumberId())
                    .recordDate(resultBO.getLastTime().toLocalDate())
                    .lastTime(resultBO.getLastTime())
                    .lastNumber(resultBO.getLastNumber())
                    .originalSequence(originalSequence)  // 这里现在可以正常使用�?
                    .count((long) resultBO.getNumberList().size())
                    .build();
            serialNumberRecordDao.insert(recordEntity);
        }
    }

    /**
     * 若不在规则周期内，重制初始�?
     *
     * @return
     */
    private boolean isResetInitNumber(SerialCodeLastGenerateBO lastGenerate, SerialCodeInfoBO serialNumberInfo) {
        LocalDateTime lastTime = lastGenerate.getLastTime();
        if (lastTime == null) {
            return true;
        }

        SerialCodeRuleTypeEnum serialNumberRuleTypeEnum = serialNumberInfo.getSerialNumberRuleTypeEnum();
        int lastTimeYear = lastTime.getYear();
        int lastTimeMonth = lastTime.getMonthValue();
        int lastTimeDay = lastTime.getDayOfYear();

        LocalDateTime now = LocalDateTime.now();

        switch (serialNumberRuleTypeEnum) {
            case YEAR:
                return lastTimeYear != now.getYear();
            case MONTH:
                return lastTimeYear != now.getYear() || lastTimeMonth != now.getMonthValue();
            case DAY:
                return lastTimeYear != now.getYear() || lastTimeDay != now.getDayOfYear();
            default:
                return false;
        }
    }

    /**
     * 生成最终编码列表（使用正则替换�?
     */
    protected List<String> formatNumberList(SerialCodeGenerateResultBO generateResult, SerialCodeInfoBO serialNumberInfo) {

        /**
         * 第一步：准备时间部分
         */
        LocalDate lastTime = generateResult.getLastTime().toLocalDate();

        // 构建替换映射
        Map<String, String> replacements = new HashMap<>();

        if (Boolean.TRUE.equals(serialNumberInfo.getHaveYearFlag())) {
            replacements.put(SerialCodeRuleTypeEnum.YEAR.getRegex(), String.valueOf(lastTime.getYear()));
        }
        if (Boolean.TRUE.equals(serialNumberInfo.getHaveMonthFlag())) {
            String month = lastTime.getMonthValue() > 9 ?
                    String.valueOf(lastTime.getMonthValue()) : "0" + lastTime.getMonthValue();
            replacements.put(SerialCodeRuleTypeEnum.MONTH.getRegex(), month);
        }
        if (Boolean.TRUE.equals(serialNumberInfo.getHaveDayFlag())) {
            String day = lastTime.getDayOfMonth() > 9 ?
                    String.valueOf(lastTime.getDayOfMonth()) : "0" + lastTime.getDayOfMonth();
            replacements.put(SerialCodeRuleTypeEnum.DAY.getRegex(), day);
        }

        /**
         * 第二步：生成编码
         */
        List<String> numberList = Lists.newArrayListWithCapacity(generateResult.getNumberList().size());
        String originalFormat = serialNumberInfo.getFormat();

        for (Long number : generateResult.getNumberList()) {
            // 格式化数�?
            String formattedNumber = formatSecureNumberOptimized(number, serialNumberInfo);

            // 先复制原始格�?
            String result = originalFormat;

            // 替换年月�?
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }

            // 替换数字部分（需要特殊处理，因为数字占位符格式可能不同）
            // 找到数字占位�?
            Pattern numberPattern = Pattern.compile("\\[(n+)\\]");
            Matcher matcher = numberPattern.matcher(result);

            if (matcher.find()) {
                // 替换第一个（也是唯一一个）数字占位�?
                String numberPlaceholder = matcher.group(0);
                result = result.replace(numberPlaceholder, formattedNumber);
            }

            numberList.add(result);
        }

        return numberList;
    }
}

