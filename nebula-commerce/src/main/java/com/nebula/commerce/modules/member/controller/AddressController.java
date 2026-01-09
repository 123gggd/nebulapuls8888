package com.nebula.commerce.modules.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.member.entity.Address;
import com.nebula.commerce.modules.member.mapper.AddressMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressMapper addressMapper;
    private final SecurityUtils securityUtils; // 注入安全工具

    @GetMapping("/list")
    public Result<List<Address>> list() {
        Long userId = securityUtils.getCurrentUserId();
        return Result.success(addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getDefaultStatus) // 默认地址排前面
                .orderByDesc(Address::getCreateTime)));
    }

    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> save(@RequestBody @Valid Address address) {
        Long userId = securityUtils.getCurrentUserId();
        address.setUserId(userId);

        // 1. 数量限制 (防止恶意刷数据)
        if (address.getId() == null) {
            Long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
            if (count >= 20) {
                return Result.error("收货地址最多添加20个");
            }
            // 2. 如果是第一个地址，自动设为默认
            if (count == 0) {
                address.setDefaultStatus(1);
            }
        }

        // 3. 越权校验 (修改时)
        if (address.getId() != null) {
            Address exist = addressMapper.selectById(address.getId());
            if (exist == null || !exist.getUserId().equals(userId)) {
                return Result.error("地址不存在或无权修改");
            }
        }

        // 4. 处理默认地址逻辑
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            clearDefault(userId);
        } else {
            // 防止 null 插入数据库
            if (address.getDefaultStatus() == null) address.setDefaultStatus(0);
        }

        if (address.getId() == null) {
            addressMapper.insert(address);
        } else {
            addressMapper.updateById(address);
        }
        return Result.success("保存成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        // MP 自动生成的 delete 语句会带上 where id=? AND user_id=?，天然防止越权
        int rows = addressMapper.delete(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId));

        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PostMapping("/default/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> setDefault(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();

        // 校验地址是否存在且属于当前用户
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId));
        if (count == 0) return Result.error("地址不存在");

        // 1. 取消所有默认
        clearDefault(userId);

        // 2. 设置当前为默认
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .set(Address::getDefaultStatus, 1)
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId));

        return Result.success("设置成功");
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .set(Address::getDefaultStatus, 0)
                .eq(Address::getUserId, userId));
    }
}