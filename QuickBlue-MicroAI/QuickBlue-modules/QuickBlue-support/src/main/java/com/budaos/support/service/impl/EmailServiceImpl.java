package com.budaos.support.service.impl;

import cn.hutool.core.util.IdUtil;
import com.budaos.support.constant.EmailTemplateCodeEnum;
import com.budaos.support.constant.EmailTemplateTypeEnum;
import com.budaos.support.dao.EmailTemplateDao;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.EmailTemplateEntity;
import com.budaos.support.service.EmailService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * 邮件服务实现
 *
 * @author budaos
 */
@Slf4j
@Component
public class EmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private EmailTemplateDao mailTemplateDao;

    @Value("${spring.mail.username:noreply@budaos.com}")
    private String clientMail;

    /**
     * 使用模板发送邮件
     */
    @Override
    public ApiResult<String> sendMail(EmailTemplateCodeEnum templateCode, Map<String, Object> templateParamsMap, List<String> receiverUserList, List<File> fileList) {
        EmailTemplateEntity mailTemplateEntity = mailTemplateDao.selectById(templateCode.name().toLowerCase());
        if (mailTemplateEntity == null) {
            return ApiResult.paramError("模版不存在");
        }

        if (mailTemplateEntity.getDisableFlag()) {
            return ApiResult.paramError("模版已禁用，无法发送");
        }

        String content = null;
        if (EmailTemplateTypeEnum.FREEMARKER.getValue().equalsIgnoreCase(mailTemplateEntity.getTemplateType().trim())) {
            content = freemarkerResolverContent(mailTemplateEntity.getTemplateContent(), templateParamsMap);
        } else if (EmailTemplateTypeEnum.STRING.getValue().equalsIgnoreCase(mailTemplateEntity.getTemplateType().trim())) {
            content = stringResolverContent(mailTemplateEntity.getTemplateContent(), templateParamsMap);
        } else {
            return ApiResult.paramError("模版类型不存在");
        }

        try {
            this.sendMail(mailTemplateEntity.getTemplateSubject(), content, fileList, receiverUserList, true);
        } catch (Throwable e) {
            log.error("邮件发送失败", e);
            return ApiResult.paramError("邮件发送失败");
        }
        return ApiResult.ok();
    }

    /**
     * 使用模板发送邮件
     */
    @Override
    public ApiResult<String> sendMail(EmailTemplateCodeEnum templateCode, Map<String, Object> templateParamsMap, List<String> receiverUserList) {
        return this.sendMail(templateCode, templateParamsMap, receiverUserList, null);
    }

    /**
     * 发送邮件
     */
    public void sendMail(String subject, String content, List<File> fileList, List<String> receiverUserList, boolean isHtml) throws MessagingException {
        if (CollectionUtils.isEmpty(receiverUserList)) {
            throw new RuntimeException("接收方不能为空");
        }

        if (StringUtils.isBlank(content)) {
            throw new RuntimeException("邮件内容不能为空");
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        //是否为多文件上传
        boolean multiparty = !CollectionUtils.isEmpty(fileList);
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multiparty);
        helper.setFrom(clientMail);
        helper.setTo(receiverUserList.toArray(new String[0]));
        helper.setSubject(subject);
        //发送html格式
        helper.setText(content, isHtml);

        //附件
        if (multiparty) {
            for (File file : fileList) {
                helper.addAttachment(file.getName(), file);
            }
        }
        javaMailSender.send(mimeMessage);
    }

    /**
     * 使用字符串生成最终内容
     */
    private String stringResolverContent(String stringTemplate, Map<String, Object> templateParamsMap) {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(templateParamsMap);
        String contractHtml = stringSubstitutor.replace(stringTemplate);
        Document doc = Jsoup.parse(contractHtml);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return doc.outerHtml();
    }

    /**
     * 使用 freemarker 生成最终内容
     */
    private String freemarkerResolverContent(String htmlTemplate, Map<String, Object> templateParamsMap) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        String templateName = IdUtil.fastSimpleUUID();
        stringLoader.putTemplate(templateName, htmlTemplate);
        configuration.setTemplateLoader(stringLoader);
        try {
            Template template = configuration.getTemplate(templateName, "utf-8");
            Writer out = new StringWriter(2048);
            template.process(templateParamsMap, out);
            return out.toString();
        } catch (Throwable e) {
            log.error("freemarkerResolverContent error: ", e);
        }
        return "";
    }
}
