package com.budaos.ai.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.ai.llm.entity.AiragModel;
import com.budaos.ai.llm.mapper.AiragModelMapper;
import com.budaos.ai.llm.service.IAiragModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI模型 Service 实现
 */
@Slf4j
@Service
public class AiragModelServiceImpl extends ServiceImpl<AiragModelMapper, AiragModel> implements IAiragModelService {

    @Override
    public List<AiragModel> listActivated() {
        return list(new LambdaQueryWrapper<AiragModel>()
                .eq(AiragModel::getActivateFlag, 1));
    }

    @Override
    public List<AiragModel> listByProvider(String provider) {
        return list(new LambdaQueryWrapper<AiragModel>()
                .eq(AiragModel::getProvider, provider)
                .eq(AiragModel::getActivateFlag, 1));
    }

    @Override
    public AiragModel getDefaultModel() {
        return getOne(new LambdaQueryWrapper<AiragModel>()
                .eq(AiragModel::getActivateFlag, 1)
                .last("LIMIT 1"));
    }
}
