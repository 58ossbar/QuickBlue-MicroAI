package com.budaos.common.core.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字符串解析工具类
 *
 * <p>提供将字符串按分隔符拆分为 Set / List / 数组 的便捷方法，
 * 支持 int、long、byte、double、float 等基本类型的容错解析。</p>
 *
 * @author budaos
 * @since 2026-01-01
 */
public class StringParseUtil extends StrUtil {

    // =============== 通用拆分（String） =======================

    public static Set<String> splitConvertToSet(String str, String split) {
        if (isEmpty(str)) {
            return new HashSet<>();
        }
        return Arrays.stream(str.split(split)).collect(Collectors.toSet());
    }

    public static List<String> splitConvertToList(String str, String split) {
        if (isEmpty(str)) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(split)).collect(Collectors.toList());
    }

    // =============== 通用拆分（基本类型，容错解析） =======================

    private static <T> List<T> splitToTypeList(String str, String split, T defaultVal, Function<String, T> parser) {
        List<T> result = new ArrayList<>();
        if (isEmpty(str)) {
            return result;
        }
        for (String item : str.split(split)) {
            result.add(parseValue(item, defaultVal, parser));
        }
        return result;
    }

    private static <T> Set<T> splitToTypeSet(String str, String split, T defaultVal, Function<String, T> parser) {
        Set<T> result = new HashSet<>();
        if (isEmpty(str)) {
            return result;
        }
        for (String item : str.split(split)) {
            result.add(parseValue(item, defaultVal, parser));
        }
        return result;
    }

    private static <T> T parseValue(String item, T defaultVal, Function<String, T> parser) {
        try {
            return parser.apply(item);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    // =============== split Integer =======================

    public static List<Integer> splitConvertToIntList(String str, String split, int defaultVal) {
        return splitToTypeList(str, split, defaultVal, Integer::parseInt);
    }

    public static Set<Integer> splitConvertToIntSet(String str, String split, int defaultVal) {
        return splitToTypeSet(str, split, defaultVal, Integer::parseInt);
    }

    public static Set<Integer> splitConvertToIntSet(String str, String split) {
        return splitConvertToIntSet(str, split, 0);
    }

    public static List<Integer> splitConvertToIntList(String str, String split) {
        return splitConvertToIntList(str, split, 0);
    }

    public static int[] splitConvertToIntArray(String str, String split, int defaultVal) {
        List<Integer> list = splitConvertToIntList(str, split, defaultVal);
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static int[] splitConvertToIntArray(String str, String split) {
        return splitConvertToIntArray(str, split, 0);
    }

    // =============== split Long =======================

    public static List<Long> splitConvertToLongList(String str, String split, long defaultVal) {
        return splitToTypeList(str, split, defaultVal, Long::parseLong);
    }

    public static List<Long> splitConvertToLongList(String str, String split) {
        return splitConvertToLongList(str, split, 0L);
    }

    public static long[] splitConvertToLongArray(String str, String split, long defaultVal) {
        List<Long> list = splitConvertToLongList(str, split, defaultVal);
        return list.stream().mapToLong(Long::longValue).toArray();
    }

    public static long[] splitConvertToLongArray(String str, String split) {
        return splitConvertToLongArray(str, split, 0L);
    }

    // =============== split Byte =======================

    public static List<Byte> splitConvertToByteList(String str, String split, byte defaultVal) {
        return splitToTypeList(str, split, defaultVal, Byte::parseByte);
    }

    public static List<Byte> splitConvertToByteList(String str, String split) {
        return splitConvertToByteList(str, split, (byte) 0);
    }

    public static byte[] splitConvertToByteArray(String str, String split, byte defaultVal) {
        List<Byte> list = splitConvertToByteList(str, split, defaultVal);
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static byte[] splitConvertToByteArray(String str, String split) {
        return splitConvertToByteArray(str, split, (byte) 0);
    }

    // =============== split Double =======================

    public static List<Double> splitConvertToDoubleList(String str, String split, double defaultVal) {
        return splitToTypeList(str, split, defaultVal, Double::parseDouble);
    }

    public static List<Double> splitConvertToDoubleList(String str, String split) {
        return splitConvertToDoubleList(str, split, 0);
    }

    public static double[] splitConvertToDoubleArray(String str, String split, double defaultVal) {
        List<Double> list = splitConvertToDoubleList(str, split, defaultVal);
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static double[] splitConvertToDoubleArray(String str, String split) {
        return splitConvertToDoubleArray(str, split, 0);
    }

    // =============== split Float =======================

    public static List<Float> splitConvertToFloatList(String str, String split, float defaultVal) {
        return splitToTypeList(str, split, defaultVal, Float::parseFloat);
    }

    public static List<Float> splitConvertToFloatList(String str, String split) {
        return splitConvertToFloatList(str, split, 0f);
    }

    public static float[] splitConvertToFloatArray(String str, String split, float defaultVal) {
        List<Float> list = splitConvertToFloatList(str, split, defaultVal);
        float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static float[] splitConvertToFloatArray(String str, String split) {
        return splitConvertToFloatArray(str, split, 0f);
    }

    // =============== 其他 =======================

    public static String upperCaseFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String replace(String content, int begin, int end, String newStr) {
        if (content == null || newStr == null || begin < 0 || begin >= content.length() || end > content.length() || begin >= end) {
            return content;
        }
        String replacement = newStr.repeat(end - begin);
        return content.substring(0, begin) + replacement + content.substring(end);
    }
}
