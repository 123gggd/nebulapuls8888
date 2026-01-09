package com.nebula.commerce.modules.product.controller;

import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.product.entity.Category;
import com.nebula.commerce.modules.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getTreeList(false));
    }

    // 修复：明确指定路径，防止 404/500
    @GetMapping({"/tree", "/admin/tree"})
    public Result<List<Category>> getTree() {
        // 如果这里报错，请检查 Service 是否有空指针
        return Result.success(categoryService.getTreeList(false));
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> add(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("分类添加成功");
    }

    @PutMapping("/admin/update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        categoryService.removeCategorySafely(id);
        return Result.success("分类删除成功");
    }
}