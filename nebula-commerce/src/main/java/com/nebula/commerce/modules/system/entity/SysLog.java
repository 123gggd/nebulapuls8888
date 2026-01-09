package com.nebula.commerce.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    // 记录 "类名.方法名" 或 Swagger 注解描述
    private String operation;

    // 请求路径 URI
    private String method;

    // 请求参数 JSON
    private String params;

    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}