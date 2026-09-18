package com.budaos.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AI模型配置 DTO
 */
@Data
public class AiModelDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    private String id;
    
    /** 名称 */
    private String name;
    
    /** 供应者 */
    private String provider;
    
    /** 模型类型 */
    private String modelType;
    
    /** 模型名称 */
    private String modelName;
    
    /** API域名 */
    private String baseUrl;
    
    /** 凭证信息 */
    private String credential;
    
    /** 模型参数 */
    private String modelParams;
    
    /** 是否激活 (0=未激活，1=已激活) */
    private Integer activateFlag;
    
    /** 创建人 */
    private String createBy;
    
    /** 创建日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /** 更新人 */
    private String updateBy;
    
    /** 更新日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
