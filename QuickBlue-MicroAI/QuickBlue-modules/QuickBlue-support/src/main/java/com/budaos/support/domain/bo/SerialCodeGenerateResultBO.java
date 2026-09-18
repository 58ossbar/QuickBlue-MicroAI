package com.budaos.support.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 单据序列号 生成结果
 *
 * @author budaos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SerialCodeGenerateResultBO {

    /**
     * 序号id
     */
    private Integer serialNumberId;

    /**
     *  是否重置的初始值
     */
    private Boolean isReset;

    /**
     * 上次生成的数字
     */
    private Long lastNumber;

    /**
     * 上次生成的时间
     */
    private LocalDateTime lastTime;

    /**
     * 生成的 number  集合
     */
    private List<Long> numberList;

    /**
     * 原始序列号集合（安全模式使用）
     */
    private List<Long> originalSequenceList;

}
