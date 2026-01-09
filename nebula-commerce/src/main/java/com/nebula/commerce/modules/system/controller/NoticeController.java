package com.nebula.commerce.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.system.annotation.Log;
import com.nebula.commerce.modules.system.entity.Notice;
import com.nebula.commerce.modules.system.mapper.NoticeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;

    // --- 前台接口 ---

    @GetMapping("/portal/notice/list")
    public Result<List<Notice>> listPortal() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1) // 仅显示已发布的
                .orderByAsc(Notice::getSort)
                .orderByDesc(Notice::getCreateTime)
                .last("LIMIT 5")));
    }

    @GetMapping("/portal/notice/detail/{id}")
    public Result<Notice> detailPortal(@PathVariable Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getStatus() != 1) {
            return Result.error("公告不存在或已下架");
        }
        return Result.success(notice);
    }

    // --- 后台管理接口 (Admin) ---

    @GetMapping("/admin/notice/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Notice>> listAdmin(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(Notice::getTitle, title);
        }
        wrapper.orderByAsc(Notice::getSort).orderByDesc(Notice::getCreateTime);
        return Result.success(noticeMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @PostMapping("/admin/notice/save")
    @PreAuthorize("hasRole('ADMIN')")
    @Log("保存/更新公告") // 记录操作日志
    public Result<String> save(@RequestBody @Valid Notice notice) {
        if (notice.getId() == null) {
            if (notice.getStatus() == null) notice.setStatus(1);
            if (notice.getSort() == null) notice.setSort(0);
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
        return Result.success("保存成功");
    }

    @DeleteMapping("/admin/notice/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Log("删除公告")
    public Result<String> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success("删除成功");
    }
}