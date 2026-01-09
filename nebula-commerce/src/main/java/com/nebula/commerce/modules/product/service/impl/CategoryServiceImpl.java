package com.nebula.commerce.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.commerce.modules.product.entity.Category;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.mapper.CategoryMapper;
import com.nebula.commerce.modules.product.mapper.ProductMapper;
import com.nebula.commerce.modules.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 * 优化点：将树构建算法从 O(n^2) 优化为 O(n)，使用 Map 分组替代递归循环
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    @Override
    public List<Category> getTreeList(boolean onlyActive) {
        // 1. 查询所有分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (onlyActive) {
            wrapper.eq(Category::getStatus, true);
        }
        // 预先排序，减少后续排序开销
        wrapper.orderByAsc(Category::getSort);
        List<Category> all = baseMapper.selectList(wrapper);

        if (all.isEmpty()) return Collections.emptyList();

        // 2. [核心优化] 按 parentId 分组，避免递归中重复遍历全表
        // Map<ParentId, List<Children>>
        Map<Long, List<Category>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        // 3. 组装树，只处理根节点 (parentId == 0 或 null)
        return all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(root -> buildTree(root, childrenMap))
                .sorted(Comparator.comparingInt(this::getSortSafe))
                .collect(Collectors.toList());
    }

    /**
     * 递归组装子节点 (基于 Map 查找，效率极高)
     */
    private Category buildTree(Category parent, Map<Long, List<Category>> childrenMap) {
        List<Category> children = childrenMap.get(parent.getId());
        if (children != null) {
            // 对子节点继续递归组装
            children.forEach(child -> buildTree(child, childrenMap));
            // 排序
            children.sort(Comparator.comparingInt(this::getSortSafe));
            parent.setChildren(children);
        }
        return parent;
    }

    private int getSortSafe(Category c) {
        return c.getSort() == null ? 0 : c.getSort();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategorySafely(Long id) {
        // 检查子分类
        Long childCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new RuntimeException("该分类下包含子分类，请先删除子分类");
        }

        // 检查关联商品
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id)
        );
        if (productCount > 0) {
            throw new RuntimeException("该分类下仍有商品，无法删除");
        }

        baseMapper.deleteById(id);
    }
}