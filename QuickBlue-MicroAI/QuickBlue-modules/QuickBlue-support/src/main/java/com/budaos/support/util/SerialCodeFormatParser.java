package com.budaos.support.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

/**
 * 编码格式解析工具
 *
 * @author budaos
 */
public class SerialCodeFormatParser {

    /**
     * 格式元素类型
     */
    public enum ElementType {
        PREFIX,     // 前缀
        YEAR,       // 年 [yyyy]
        MONTH,      // 月 [mm]
        DAY,        // 日 [dd]
        NUMBER,     // 数字 [n...]
        SUFFIX      // 后缀
    }

    /**
     * 格式元素
     */
    public static class FormatElement {
        private ElementType type;
        private String value;
        private int length; // 对于数字，表示位数

        public FormatElement(ElementType type, String value, int length) {
            this.type = type;
            this.value = value;
            this.length = length;
        }

        // getters
        public ElementType getType() { return type; }
        public String getValue() { return value; }
        public int getLength() { return length; }
    }

    /**
     * 解析格式字符串
     */
    public static List<FormatElement> parseFormat(String format) {
        List<FormatElement> elements = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\[yyyy\\]|\\[mm\\]|\\[dd\\]|\\[n+\\]|[^\\[\\]]+)");
        Matcher matcher = pattern.matcher(format);

        while (matcher.find()) {
            String part = matcher.group(1);

            if (part.equals("[yyyy]")) {
                elements.add(new FormatElement(ElementType.YEAR, part, 4));
            } else if (part.equals("[mm]")) {
                elements.add(new FormatElement(ElementType.MONTH, part, 2));
            } else if (part.equals("[dd]")) {
                elements.add(new FormatElement(ElementType.DAY, part, 2));
            } else if (part.startsWith("[n") && part.endsWith("]")) {
                int nLength = part.length() - 2; // 去掉 [ 和 ]
                elements.add(new FormatElement(ElementType.NUMBER, part, nLength));
            } else {
                // 普通文本（前缀或后缀）
                elements.add(new FormatElement(ElementType.PREFIX, part, part.length()));
            }
        }

        return elements;
    }

    /**
     * 验证格式是否合法
     */
    public static boolean validateFormat(String format) {
        // 必须包含数字占位符
        if (!format.matches(".*\\[n+\\].*")) {
            return false;
        }

        // 检查占位符格式
        if (!format.matches("^[^\\[\\]]*(\\[[^\\[\\]]+\\][^\\[\\]]*)*$")) {
            return false;
        }

        return true;
    }

    /**
     * 获取数字位数
     */
    public static int getNumberLength(String format) {
        Pattern pattern = Pattern.compile("\\[(n+)\\]");
        Matcher matcher = pattern.matcher(format);
        if (matcher.find()) {
            return matcher.group(1).length();
        }
        return 0;
    }

    /**
     * 生成编码示例
     */
    public static String generateExample(String format, String year, String month, String day, String number) {
        String result = format;

        // 替换年月日
        result = result.replace("[yyyy]", year != null ? year : "2024");
        result = result.replace("[mm]", month != null ? month : "01");
        result = result.replace("[dd]", day != null ? day : "01");

        // 替换数字
        Pattern numberPattern = Pattern.compile("\\[(n+)\\]");
        Matcher numberMatcher = numberPattern.matcher(result);
        if (numberMatcher.find()) {
            String nPart = numberMatcher.group(0);
            String formattedNumber = String.format("%0" + (nPart.length() - 2) + "d",
                    Long.parseLong(number != null ? number : "1"));
            result = result.replace(nPart, formattedNumber);
        }

        return result;
    }
}
