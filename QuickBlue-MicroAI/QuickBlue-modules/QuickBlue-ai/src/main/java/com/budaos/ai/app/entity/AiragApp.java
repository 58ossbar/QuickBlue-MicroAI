package com.budaos.ai.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI应用实体
 */
@Data
@TableName("airag_app")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "AI应用")
public class AiragApp implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /** 创建人 */
    @Schema(description = "创建人")
    private String createBy;

    /** 创建日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private java.util.Date createTime;

    /** 更新人 */
    @Schema(description = "更新人")
    private String updateBy;

    /** 更新日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private java.util.Date updateTime;

    /** 所属部门 */
    @Schema(description = "所属部门")
    private String sysOrgCode;

    /** 租户id */
    @Schema(description = "租户id")
    private String tenantId;

    /** 应用名称 */
    @NotBlank(message = "应用名称不能为空")
    @Length(max = 100, message = "应用名称不能超过100个字符")
    @Schema(description = "应用名称")
    private String name;

    /** 应用描述 */
    @Length(max = 500, message = "应用描述不能超过500个字符")
    @Schema(description = "应用描述")
    private String descr;

    /** 应用图标 */
    @Schema(description = "应用图标")
    private String icon;

    /** 应用类型 */
    @NotBlank(message = "应用类型不能为空")
    @Schema(description = "应用类型")
    private String type;

    /** 开场白 */
    @Schema(description = "开场白")
    private String prologue;

    /** 预设问题 */
    @Schema(description = "预设问题")
    private String presetQuestion;

    /** 提示词 */
    @Schema(description = "提示词")
    private String prompt;

    /** 模型配置 */
    @Schema(description = "模型配置")
    private String modelId;

    /** 历史消息数 */
    @Max(value = 50, message = "历史消息数不能超过50")
    @Schema(description = "历史消息数")
    private Integer msgNum;

    /** 知识库 */
    @Schema(description = "知识库")
    private String knowledgeIds;

    /** 流程 */
    @Schema(description = "流程")
    private String flowId;

    /** 快捷指令 */
    @Schema(description = "快捷指令")
    private String quickCommand;

    /** 状态（enable=启用、disable=禁用、release=发布） */
    @Schema(description = "状态")
    private String status;

    /** 元数据 */
    @Schema(description = "元数据")
    private String metadata;

    /** 插件 [{pluginId: '123213', pluginName: 'xxxx', category: 'mcp'}] */
    @Schema(description = "插件")
    private String plugins;

    /** 是否开启记忆(0 不开启，1开启) */
    @Schema(description = "是否开启记忆(0 不开启，1开启)")
    private Integer izOpenMemory;

    /** 记忆库，知识库的id */
    @Schema(description = "记忆库")
    private String memoryId;

    /** 变量 */
    @Schema(description = "变量")
    private String variables;

    /** 记忆和变量提示词 */
    @Schema(description = "记忆和变量提示词")
    private String memoryPrompt;

    /** 知识库ids */
    @TableField(exist = false)
    private List<String> knowIds;

    /**
     * 获取知识库id列表
     */
    public List<String> getKnowIds() {
        if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
            String[] ids = knowledgeIds.split(",");
            return Arrays.asList(ids);
        } else {
            return new ArrayList<>(0);
        }
    }
}
