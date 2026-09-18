package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.EmailTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 邮件模板 DAO
 *
 * @author budaos
 */
@Mapper
public interface EmailTemplateDao extends BaseMapper<EmailTemplateEntity> {

}
