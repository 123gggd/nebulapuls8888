package com.nebula.commerce.modules.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password; // 加密后的密码
    private String nickname;
    private String avatar;

    /**
     * 角色: ADMIN, USER
     * [Phase 2] 新增: MERCHANT (商家管理员)
     * [修复] 映射到数据库的 role_code 列，解决 Unknown column 'role' 错误
     */
    @TableField("role_code")
    private String role;

    /**
     * [Phase 2] 多商户字段
     * 如果 role="MERCHANT"，此字段存储关联的 sys_merchant 表 ID。
     * 对于普通用户或平台管理员，此字段为 null 或 0。
     */
    private Long merchantId;

    // [Phase 7 新增] 状态: 1启用 0禁用
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}