package com.budaos.common.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean拷贝工具类
 *
 * @author budaos
 */
public class BeanCopyUtil {

    /**
     * 验证器
     */
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 拷贝对象属性
     *
     * @param source 源对象
     * @param target 目标类
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, target);
    }

    /**
     * 拷贝对象属性（Spring BeanUtils）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 拷贝对象属性（别名方法）
     *
     * @param source 源对象
     * @param target 目标类
     * @return 目标对象
     */
    public static <T> T copy(Object source, Class<T> target) {
        return copyProperties(source, target);
    }

    /**
     * 拷贝列表
     *
     * @param sourceList 源列表
     * @param targetClass 目标类
     * @return 目标列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(source -> BeanUtil.copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 拷贝列表（自定义转换函数）
     *
     * @param sourceList 源列表
     * @param mapper     转换函数
     * @return 目标列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Function<S, T> mapper) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 手动验证对象Model的属性
     * 需要配合hibernate-validator校验注解
     *
     * @param t 要验证的对象
     * @return String 返回null代表验证通过，否则返回错误的信息
     */
    public static <T> String verify(T t) {
        // 获取验证结果
        Set<ConstraintViolation<T>> validate = VALIDATOR.validate(t);
        if (validate.isEmpty()) {
            // 验证通过
            return null;
        }
        // 返回错误信息
        List<String> messageList = validate.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
        return messageList.toString();
    }
}
