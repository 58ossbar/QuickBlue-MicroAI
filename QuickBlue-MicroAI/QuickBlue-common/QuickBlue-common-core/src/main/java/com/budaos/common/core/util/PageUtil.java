package com.budaos.common.core.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;

import java.util.List;

/**
 * 分页工具类（简化版）
 *
 * @author budaos
 * @since 2026-02-11
 */
@Deprecated
public class PageUtil {

    /**
     * 将分页参数转换为MyBatis-Plus的Page对象
     *
     * @param pageParam 分页参数
     * @return Page对象
     */
    public static <T> Page<T> convert2PageQuery(PageQuery pageParam) {
        return PageConvertUtil.convert2PageQuery(pageParam);
    }

    /**
     * 将分页参数转换为MyBatis-Plus的Page对象
     *
     * @param current  当前页
     * @param pageSize 每页大小
     * @return Page对象
     */
    public static <T> Page<T> of(long current, long pageSize) {
        return new Page<>(current, pageSize);
    }

    /**
     * 将MyBatis-Plus的Page对象转换为PageResult
     *
     * @param page MyBatis-Plus的Page对象
     * @return PageResult对象
     */
    public static <T> PageResponse<T> convert2PageResult(Page<T> page) {
        if (page == null) {
            return new PageResponse<>();
        }
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    /**
     * 将MyBatis-Plus的Page对象转换为PageResult（自定义数据）
     *
     * @param page MyBatis-Plus的Page对象
     * @param data 自定义数据列表
     * @return PageResult对象
     */
    public static <T> PageResponse<T> convert2PageResult(Page<?> page, List<T> data) {
        if (page == null) {
            return new PageResponse<>();
        }
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), data);
    }
}
