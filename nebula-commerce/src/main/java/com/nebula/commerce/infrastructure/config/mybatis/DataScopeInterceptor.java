package com.nebula.commerce.infrastructure.config.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.stereotype.Component;

/**
 * 商家数据权限拦截器
 * 优化：直接继承官方稳定的 TenantLineInnerInterceptor
 * * 不再需要手动解析 SQL (JSqlParser)，官方拦截器已经完美处理了
 * SELECT, UPDATE, DELETE 语句的条件追加，以及复杂的嵌套查询。
 */
@Component
public class DataScopeInterceptor extends TenantLineInnerInterceptor {

    /**
     * 构造函数注入我们的逻辑处理器 DataScopeHandler
     */
    public DataScopeInterceptor(DataScopeHandler dataScopeHandler) {
        super(dataScopeHandler);
    }

    // 这里不需要重写任何方法，TenantLineInnerInterceptor 会自动调用
    // DataScopeHandler 中的 getTenantId() 和 ignoreTable() 方法
}