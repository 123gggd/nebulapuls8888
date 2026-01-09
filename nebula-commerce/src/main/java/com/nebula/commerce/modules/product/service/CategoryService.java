package com.nebula.commerce.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.commerce.modules.product.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树形结构
     * @param onlyActive 是否只包含启用的分类
     * @return 树形列表
     */
    List<Category> getTreeList(boolean onlyActive);

    /**
     * 安全删除分类
     * @param id 分类ID
     */
    void removeCategorySafely(Long id);
}