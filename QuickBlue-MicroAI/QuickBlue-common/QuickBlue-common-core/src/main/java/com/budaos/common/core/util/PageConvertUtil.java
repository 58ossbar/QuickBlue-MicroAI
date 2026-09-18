package com.budaos.common.core.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageQuery;
import com.budaos.common.core.domain.PageResponse;

import java.util.List;

/**
 * 分页工具类
 *
 * @author budaos
 * @since 2026-02-11
 */
public class PageConvertUtil {

    /**
     * 将分页参数转换为MyBatis-Plus的Page对象
     *
     * @param pageParam 分页参数
     * @return Page对象
     */
    public static <T> Page<T> convert2PageQuery(PageQuery pageParam) {
        Page<T> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());

        // 处理排序
        if (pageParam.getSortItemList() != null && !pageParam.getSortItemList().isEmpty()) {
            for (PageQuery.SortItem sortItem : pageParam.getSortItemList()) {
                boolean isAsc = sortItem.getIsAsc() != null ? sortItem.getIsAsc() : true;
                String column = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(sortItem.getColumn());
                
                if (isAsc) {
                    page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.asc(column));
                } else {
                    page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.desc(column));
                }
            }
        }

        return page;
    }

    /**
     * 将MyBatis-Plus的Page对象转换为分页结果
     *
     * @param page    MyBatis-Plus的Page对象
     * @param dataList 数据列表
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResponse<T> convert2PageResult(Page<?> page, List<T> dataList) {
        PageResponse<T> pageResult = new PageResponse<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setDataList(dataList);
        return pageResult;
    }

    /**
     * 将MyBatis-Plus的Page对象转换为分页结果
     *
     * @param page MyBatis-Plus的Page对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResponse<T> convert2PageResult(Page<T> page) {
        PageResponse<T> pageResult = new PageResponse<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setDataList(page.getRecords());
        return pageResult;
    }
}
