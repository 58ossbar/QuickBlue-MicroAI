package com.budaos.support.service;

import com.budaos.support.constant.EmailTemplateCodeEnum;
import com.budaos.common.core.domain.ApiResult;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 邮件服务
 *
 * @author budaos
 */
public interface EmailService {

    /**
     * 使用模板发送邮件
     *
     * @param templateCode      模板编码
     * @param templateParamsMap 模板参数
     * @param receiverUserList  接收方列表
     * @param fileList          文件列表
     * @return 发送结果
     */
    ApiResult<String> sendMail(EmailTemplateCodeEnum templateCode, Map<String, Object> templateParamsMap, List<String> receiverUserList, List<File> fileList);

    /**
     * 使用模板发送邮件（无附件）
     *
     * @param templateCode      模板编码
     * @param templateParamsMap 模板参数
     * @param receiverUserList  接收方列表
     * @return 发送结果
     */
    ApiResult<String> sendMail(EmailTemplateCodeEnum templateCode, Map<String, Object> templateParamsMap, List<String> receiverUserList);
}
