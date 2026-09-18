package com.budaos.common.database.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis Plus 配置
 *
 * @author budaos
 */
@Configuration
public class MybatisPlusConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private ObjectProvider<SqlSessionFactory> sqlSessionFactoryProvider;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 标志位，防止重复注册拦截器
     */
    private volatile boolean registered = false;

    /**
     * MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(1000L);
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防止全表更新和删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 注册原生 MyBatis 拦截器
     * 用于数据权限插件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止重复注册
        if (registered) {
            return;
        }
        registered = true;

        try {
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryProvider.getIfAvailable();
            if (sqlSessionFactory == null) {
                return;
            }

            // 使用反射创建数据权限拦截器，避免模块依赖问题
            Class<?> pluginClass = Class.forName("com.budaos.system.plugin.MyBatisDataPermissionPlugin");
            Object dataScopePlugin = pluginClass.getDeclaredConstructor().newInstance();

            // 手动设置 ApplicationContext
            try {
                pluginClass.getMethod("setApplicationContext", ApplicationContext.class)
                        .invoke(dataScopePlugin, applicationContext);
            } catch (NoSuchMethodException e) {
                // 如果没有 setApplicationContext 方法，使用 Spring 的 AutowireAnnotationBeanPostProcessor
                System.err.println("MyBatisDataPermissionPlugin has no setApplicationContext method, skipping injection");
            }

            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            configuration.addInterceptor((org.apache.ibatis.plugin.Interceptor) dataScopePlugin);

            System.out.println("Successfully registered MyBatisDataPermissionPlugin");
        } catch (ClassNotFoundException e) {
            // 类不存在，跳过注册（可能数据权限模块未启用）
            System.out.println("MyBatisDataPermissionPlugin class not found, skipping data scope registration");
        } catch (Exception e) {
            // 静默处理异常，避免影响应用启动
            System.err.println("Failed to register MyBatis interceptors: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
