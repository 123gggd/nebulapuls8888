package com.nebula.commerce.modules.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_category")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId;

    private Integer sort;

    private Boolean status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 子分类列表
     * exist = false 表示该字段不在数据库表中，仅用于业务逻辑（构建树形结构）
     */
    @TableField(exist = false)
    private List<Category> children;
}