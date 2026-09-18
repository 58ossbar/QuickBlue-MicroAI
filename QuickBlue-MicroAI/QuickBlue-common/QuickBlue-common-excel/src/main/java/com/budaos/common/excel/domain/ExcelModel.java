package com.budaos.common.excel.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import java.io.Serializable;
import java.util.Date;

/**
 * Excel 数据模型基类
 * <p>
 * 所有需要导出的 Excel VO 类建议继承此基类，自动包含 id、创建时间、更新时间等通用字段
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
@ContentRowHeight(20)
@HeadRowHeight(25)
public class ExcelModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（默认不导出）
     */
    @ExcelIgnore
    private Long id;

    /**
     * 创建时间
     */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private Date createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private Date updateTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
