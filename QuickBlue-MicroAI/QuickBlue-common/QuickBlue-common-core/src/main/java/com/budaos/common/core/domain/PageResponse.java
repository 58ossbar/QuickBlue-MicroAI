package com.budaos.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 *
 * @author budaos
 * @since 2026-02-11
 */
@Data
@Schema(description = "分页结果")
public class PageResponse<T> {

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页数量")
    private Long pageSize;

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总页数")
    private Long pageTotal;

    public PageResponse() {
    }

    public PageResponse(Long pageNum, Long pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
    }

    /**
     * 设置数据列表
     *
     * @param dataList 数据列表
     */
    public void setDataList(List<T> dataList) {
        this.list = dataList;
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    public List<T> getDataList() {
        return this.list;
    }
}
