package com.budaos.system.domain.vo;

import com.budaos.system.domain.vo.AuthRoleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色选择VO
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色选择VO")
public class AuthRoleSelectedVO extends AuthRoleVO {

    @Schema(description = "是否被选中")
    private Boolean selected;
}
