package com.nebula.commerce.modules.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品 SKU (库存量单位)
 */
@Data
@TableName(value = "pms_sku", autoResultMap = true)
public class ProductSku {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId; // 关联 SPU ID

    private String code;

    // 销售属性组合
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> specs;

    @NotNull(message = "SKU价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    private BigDecimal price;

    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    private String picture;
}