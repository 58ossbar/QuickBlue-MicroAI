package com.budaos.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.budaos.common.core.util.RequestContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 *
 * @author budaos
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";
    private static final String CREATE_USER_ID = "createUserId";
    private static final String UPDATE_USER_ID = "updateUserId";

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ...");

        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间
        this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);

        // 填充更新时间
        this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);

        // 填充创建人ID（从RequestUser获取）
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, CREATE_USER_ID, Long.class, currentUserId);
            this.strictInsertFill(metaObject, UPDATE_USER_ID, Long.class, currentUserId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ...");

        // 填充更新时间
        this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());

        // 填充更新人ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, UPDATE_USER_ID, Long.class, currentUserId);
        }
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        try {
            Long userId = RequestContextUtil.getRequestUserId();
            if (userId == null) {
                log.warn("获取到的用户ID为null，返回默认值0");
                return 0L; // 返回默认值，可根据业务需要调整
            }
            return userId;
        } catch (Exception e) {
            log.error("获取当前用户ID失败: {}", e.getMessage(), e);
            return 0L; // 默认返回0，请根据业务需要调整
        }
    }
}
