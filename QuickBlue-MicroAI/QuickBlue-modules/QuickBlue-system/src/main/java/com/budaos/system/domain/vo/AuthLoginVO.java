package com.budaos.system.domain.vo;

import com.budaos.system.domain.SessionEmployee;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录结果信息
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录结果信息")
public class AuthLoginVO extends SessionEmployee {

    @Schema(description = "token")
    private String token;

    @Schema(description = "菜单列表")
    private List<NavMenuVO> menuList;

    @Schema(description = "是否需要修改密码")
    private Boolean needUpdatePwdFlag;

    @Schema(description = "上次登录ip")
    private String lastLoginIp;

    @Schema(description = "上次登录ip地区")
    private String lastLoginIpRegion;

    @Schema(description = "上次登录user-agent")
    private String lastLoginUserAgent;

    @Schema(description = "上次登录时间")
    private LocalDateTime lastLoginTime;

}
