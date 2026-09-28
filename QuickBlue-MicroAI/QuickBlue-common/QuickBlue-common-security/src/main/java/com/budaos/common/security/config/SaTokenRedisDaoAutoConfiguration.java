package com.budaos.common.security.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.TimeUnit;

/**
 * Sa-Token Redis DAO 修正
 * <p>
 * Sa-Token 1.46 的 {@code SaTokenDaoForRedisTemplate#setStringAndKeepTTL} 会先查询 key 的剩余有效期，
 * 再以 {@code SET key value PX <ttl> XX} 的方式"更新 value 并保持原有过期时间"。
 * 但 {@code getTimeout} 在 key 不存在时返回 {@code NOT_VALUE_EXPIRE(-2)}、永不过期时返回 {@code NEVER_EXPIRE(-1)}，
 * 原实现把这两个值当成真实 TTL 传递（例如 -2 秒 -> PX -2000），Redis 直接拒绝：
 * {@code ERR invalid expire time in set}，导致登录写入 token / session 失败。
 * </p>
 * <p>
 * 这里在应用启动完成后把 Sa-Token 的全局 DAO 替换为修正实现：-1 / -2 两种情况改为不带 TTL 的
 * {@code SET key value XX}（key 已存在则更新并保持不过期；key 不存在时因 XX 语义不写入，
 * 与 SaTokenDao 契约中"timeout <= -2 时不存储"一致）。
 * </p>
 *
 * @author QuickBlue
 */
@Slf4j
@AutoConfiguration
public class SaTokenRedisDaoAutoConfiguration {

    /**
     * 在 ApplicationRunner 阶段替换 DAO：此时 Redis 连接工厂已就绪，
     * 且晚于 Sa-Token 自身自动配置的装配时机，避免 @ConditionalOnBean 的评估时序问题。
     */
    @Bean
    public SaTokenRedisDaoFixer saTokenRedisDaoFixer(ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
        return new SaTokenRedisDaoFixer(connectionFactoryProvider);
    }

    static class SaTokenRedisDaoFixer implements ApplicationRunner {

        private final ObjectProvider<RedisConnectionFactory> connectionFactoryProvider;

        SaTokenRedisDaoFixer(ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
            this.connectionFactoryProvider = connectionFactoryProvider;
        }

        @Override
        public void run(ApplicationArguments args) {
            RedisConnectionFactory factory = connectionFactoryProvider.getIfAvailable();
            if (factory == null) {
                log.warn("未检测到 RedisConnectionFactory，跳过 Sa-Token DAO 修正");
                return;
            }
            SaTokenDaoForRedisTemplate dao = new KeepTtlSafeSaTokenDao();
            dao.init(factory);
            SaManager.setSaTokenDao(dao);
            log.info("已启用 Sa-Token Redis DAO 修正（避免 -1/-2 被当成 TTL 写入 Redis）");
        }
    }

    /**
     * 修正"保持 TTL 更新"的实现：直接用 Redis 的剩余毫秒 TTL，
     * 负数（key 不存在 / 永不过期）时退化为不带 TTL 的 SET ... XX，不再调用存在缺陷的父类实现。
     */
    static class KeepTtlSafeSaTokenDao extends SaTokenDaoForRedisTemplate {

        @Override
        public void setStringAndKeepTTL(String key, String value) {
            safeSetKeepTtl(key, value);
        }

        @Override
        public void update(String key, String value) {
            safeSetKeepTtl(key, value);
        }

        /**
         * 更新 value 并保持原有过期时间：
         * <ul>
         *     <li>剩余 TTL &gt; 0：SET key value PX ttl XX</li>
         *     <li>剩余 TTL &lt;= 0（key 不存在返回 -2、永不过期返回 -1）：SET key value XX，
         *         不存在时因 XX 不写入，存在则更新且保持永不过期</li>
         * </ul>
         */
        private void safeSetKeepTtl(String key, String value) {
            StringRedisTemplate template = this.stringRedisTemplate;
            String wrappedKey = wrapKey(key);
            byte[] rawKey = template.getStringSerializer().serialize(wrappedKey);
            byte[] rawValue = template.getStringSerializer().serialize(value);
            if (rawKey == null || rawValue == null) {
                return;
            }

            Long remainMillis = template.getExpire(wrappedKey, TimeUnit.MILLISECONDS);
            Expiration expiration = (remainMillis == null || remainMillis <= 0)
                    ? Expiration.persistent()
                    : Expiration.milliseconds(remainMillis);

            template.execute((RedisCallback<Boolean>) connection -> {
                connection.stringCommands().set(rawKey, rawValue, expiration, SetOption.SET_IF_PRESENT);
                return Boolean.TRUE;
            });
        }
    }
}
