package com.nebula.commerce.infrastructure.config.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.nebula.commerce.infrastructure.security.LoginUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 商家数据围栏处理器 (核心逻辑)
 * 优化：使用 TenantLineHandler 替代 MultiDataPermissionHandler
 * 原因：TenantLineHandler 是 MP 专门为"merchant_id = ?"这种租户模式设计的，
 * 它能更完美地处理 Join 查询、子查询以及 Update/Delete 语句的防越权。
 */
@Component
public class DataScopeHandler implements TenantLineHandler {

    // 必须进行隔离的表白名单 (表名全部小写)
    // 只有在此列表中的表，并且当前用户是商家时，才会自动拼接 merchant_id = ?
    private static final List<String> TABLE_WHITELIST = Arrays.asList(
            "sys_product",   // 商品表
            "sys_order",     // 订单表
            "mkt_coupon",    // 优惠券表
            "mkt_seckill"    // 秒杀活动
    );

    /**
     * 获取当前“租户”ID (即商家ID)
     */
    @Override
    public Expression getTenantId() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null && loginUser.isMerchant() && loginUser.getMerchantId() != null) {
            return new LongValue(loginUser.getMerchantId());
        }
        // 如果无法获取ID (如管理员)，返回 NullValue 配合 ignoreTable 使用
        return new NullValue();
    }

    /**
     * 数据库中隔离字段的名称
     */
    @Override
    public String getTenantIdColumn() {
        return "merchant_id";
    }

    /**
     * 判断当前表是否需要忽略过滤 (返回 true 则不加 WHERE 条件)
     */
    @Override
    public boolean ignoreTable(String tableName) {
        LoginUser loginUser = getLoginUser();

        // 1. 如果未登录，忽略（或根据安全策略抛出异常，通常忽略以便登录接口可用）
        if (loginUser == null) {
            return true;
        }

        // 2. 超级管理员拥有上帝视角，忽略所有过滤
        if (loginUser.isAdmin()) {
            return true;
        }

        // 3. 只有商家账号需要隔离，普通用户/后台用户忽略
        if (!loginUser.isMerchant()) {
            return true;
        }

        // 4. 如果表不在白名单中，说明该表是公共表（如 sys_dict, sys_menu），忽略过滤
        if (!TABLE_WHITELIST.contains(tableName.toLowerCase())) {
            return true;
        }

        // 5. 默认情况：需要过滤
        return false;
    }

    /**
     * 辅助方法：获取当前登录用户
     */
    private LoginUser getLoginUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
                return (LoginUser) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // 忽略非Web上下文中的异常
        }
        return null;
    }
}
