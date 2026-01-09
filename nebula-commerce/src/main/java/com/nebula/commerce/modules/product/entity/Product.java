package com.nebula.commerce.modules.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@TableName(value = "sys_product", autoResultMap = true)
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 100, message = "商品名称长度需在2-100字之间")
    private String name;

    @NotBlank(message = "副标题不能为空")
    private String subtitle;

    private String description;

    @NotNull(message = "展示价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    private BigDecimal price; // 列表展示用的最低价

    private BigDecimal originalPrice;

    @Min(value = 0, message = "库存不能为负数")
    private Integer stock; // 总库存

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    // 数据隔离字段 (自动处理)
    private Long merchantId;

    @NotBlank(message = "主图不能为空")
    private String mainImage;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> subImages;

    private Integer status; // 1:上架 0:下架

    /**
     * 动态规格定义 (用于前端渲染表头)
     * 示例：{"颜色": ["红", "蓝"], "内存": ["8G", "16G"]}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> specifications;

    // --- 以下字段不存入 product 表，仅用于参数传递 ---

    /**
     * SKU 列表 (接收前端提交的 SKU 组合)
     * 级联校验：@Valid 确保 List 中的 ProductSku 也会被校验
     */
    @TableField(exist = false)
    @Valid
    private List<ProductSku> skuList;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}