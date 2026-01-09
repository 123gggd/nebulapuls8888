package com.nebula.commerce.modules.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价实体
 * 优化点：增加商家隔离字段、商品规格快照、商家回复功能
 */
@Data
@TableName(value = "sys_review", autoResultMap = true) // 开启 ResultMap 以支持 JSON 字段
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;

    // --- 用户维度 ---
    private Long userId;

    private String userName;   // 快照：评论时的用户昵称

    private String userAvatar; // 快照：评论时的用户头像

    // --- 商品维度 ---
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    // [新增] 商品信息快照 (列表页展示用，避免联表查询)
    private String productName;

    private String productImage; // 购买时的商品主图或SKU图片

    private String specs;        // [新增] 购买规格字符串 (如: "颜色:星际白; 内存:1TB")

    @NotNull(message = "关联订单不能为空")
    private Long orderId;        // 关联订单ID

    // --- 权限与隔离 ---
    // [核心] 商家ID：用于 DataScopeHandler 自动过滤，商家只能看自己商品的评论
    private Long merchantId;

    // --- 评价内容 ---
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;      // 1-5分

    @NotBlank(message = "评价内容不能为空")
    private String content;      // 评价内容

    // 晒图字段，存储图片URL JSON数组
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    // --- 商家互动 ---
    // [新增] 商家回复内容
    private String replyContent;

    // [新增] 商家回复时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime replyTime;

    // --- 状态 ---
    // 0:隐藏/审核未通过 1:显示 2:置顶/精选
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}