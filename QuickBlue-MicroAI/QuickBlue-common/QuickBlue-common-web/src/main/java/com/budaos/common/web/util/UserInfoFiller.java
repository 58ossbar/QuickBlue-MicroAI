package com.budaos.common.web.util;

import com.budaos.common.core.domain.BaseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 通用VO用户信息填充器
 *
 * @author budaos
 * @since 2026-02-11
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserInfoFiller {

    // TODO: 注入 StaffService 或相关的 Feign 客户端
    // private final StaffService employeeService;

    /**
     * 为BaseVO列表填充用户姓名
     *
     * @param voList 继承BaseVO的对象列表
     * @param <T>    BaseVO或其子类
     */
    public <T extends BaseVO> void fillUserInfo(List<T> voList) {
        if (voList == null || voList.isEmpty()) {
            log.debug("fillUserInfo: VO列表为空");
            return;
        }

        // 收集所有用户ID
        Set<Long> userIds = new HashSet<>();
        for (T vo : voList) {
            if (vo.getCreateUserId() != null) {
                userIds.add(vo.getCreateUserId());
            }
            if (vo.getUpdateUserId() != null) {
                userIds.add(vo.getUpdateUserId());
            }
        }

        log.debug("fillUserInfo: 收集到的userIds={}, VO数量={}", userIds, voList.size());
        if (userIds.isEmpty()) {
            log.debug("fillUserInfo: 用户ID集合为空");
            return;
        }

        // 批量查询用户姓名
        // Map<Long, String> userNames = employeeService.getUserNamesByIds(userIds);
        // TODO: 实现用户姓名查询逻辑
        Map<Long, String> userNames = new HashMap<>(); // 临时空实现

        log.debug("fillUserInfo: 查询到的userNames={}", userNames);

        // 填充用户姓名
        int filledCount = 0;
        for (T vo : voList) {
            if (vo.getCreateUserId() != null) {
                String name = userNames.get(vo.getCreateUserId());
                vo.setCreateUserName(name);
                if (name != null) filledCount++;
                log.debug("fillUserInfo: VO[{}] createUserId={}, createUserName={}",
                        vo.getId(), vo.getCreateUserId(), name);
            }
            if (vo.getUpdateUserId() != null) {
                String name = userNames.get(vo.getUpdateUserId());
                vo.setUpdateUserName(name);
                if (name != null) filledCount++;
                log.debug("fillUserInfo: VO[{}] updateUserId={}, updateUserName={}",
                        vo.getId(), vo.getUpdateUserId(), name);
            }
        }
        log.debug("fillUserInfo: 成功填充{}个用户姓名", filledCount);
    }

    /**
     * 为单个BaseVO填充用户姓名
     *
     * @param vo 继承BaseVO的对象
     * @param <T> BaseVO或其子类
     */
    public <T extends BaseVO> void fillUserInfo(T vo) {
        if (vo == null) {
            return;
        }

        Set<Long> userIds = new HashSet<>();
        if (vo.getCreateUserId() != null) {
            userIds.add(vo.getCreateUserId());
        }
        if (vo.getUpdateUserId() != null) {
            userIds.add(vo.getUpdateUserId());
        }

        if (userIds.isEmpty()) {
            return;
        }

        // Map<Long, String> userNames = employeeService.getUserNamesByIds(userIds);
        // TODO: 实现用户姓名查询逻辑
        Map<Long, String> userNames = new HashMap<>(); // 临时空实现

        if (vo.getCreateUserId() != null) {
            vo.setCreateUserName(userNames.get(vo.getCreateUserId()));
        }
        if (vo.getUpdateUserId() != null) {
            vo.setUpdateUserName(userNames.get(vo.getUpdateUserId()));
        }
    }
}
