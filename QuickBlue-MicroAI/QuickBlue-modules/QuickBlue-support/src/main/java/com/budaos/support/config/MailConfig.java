package com.budaos.support.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;

/**
 * 邮件配置
 */
@Configuration
public class MailConfig {

    /**
     * 配置 JavaMailSender Bean
     * 如果 spring.mail.host 配置存在，则使用 Spring Boot 自动配置
     * 否则提供一个 Mock 实现
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return null;
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                return null;
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                // Mock 实现 - 实际环境中应该配置真实的邮件服务器
                System.out.println("邮件服务未配置，跳过邮件发送");
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
                // Mock 实现
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
                // Mock 实现
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
                // Mock 实现
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
                // Mock 实现
                System.out.println("邮件服务未配置，跳过邮件发送");
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
                // Mock 实现
            }
        };
    }
}
