package com.budaos.support.constant;

import com.budaos.common.core.domain.BaseEnumeration;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单据序列号 安全模式
 *
 * @author budaos
 */
@AllArgsConstructor
@Getter
public enum SerialCodeSecureModeEnum implements BaseEnumeration {

    NONE(0, "普通模式", "连续数字"),
    RANDOM(1, "随机模式", "随机跳跃"),
    TIMESTAMP(2, "时间戳模式", "时间戳+序列"),
    ENCRYPTED(3, "加密模式", "可逆加密");

    private final Integer value;
    private final String desc;
    private final String remark;
}
