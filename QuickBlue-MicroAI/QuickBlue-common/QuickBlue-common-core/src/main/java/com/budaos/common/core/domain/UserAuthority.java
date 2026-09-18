package com.budaos.common.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * sa-token 所需的权限信息
 *
*/

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAuthority implements Serializable {

    /**
     * 权限列表
     */
    private List<String> permissionList;

    /**
     * 角色列表
     */
    private List<String> roleList;


}
