package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.UserAddress;
import com.xyjy.mapper.UserAddressMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 收货地址管理接口
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private UserAddressMapper userAddressMapper;

    /**
     * 我的收货地址列表
     */
    @GetMapping("/list/{userId}")
    public Result<List<UserAddress>> list(@PathVariable Long userId) {
        return Result.success(userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime)));
    }

    /**
     * 新增或编辑收货地址
     */
    @PostMapping("/save")
    public Result<UserAddress> save(@RequestBody UserAddress addr) {
        if (addr.getUserId() == null) {
            throw new BusinessException("缺少用户ID");
        }
        if (addr.getReceiver() == null || addr.getReceiver().isEmpty()) {
            throw new BusinessException("请填写收货人");
        }
        if (addr.getPhone() == null || addr.getPhone().isEmpty()) {
            throw new BusinessException("请填写联系电话");
        }
        if (addr.getAddress() == null || addr.getAddress().isEmpty()) {
            throw new BusinessException("请填写收货地址");
        }
        if (addr.getId() == null) {
            if (addr.getIsDefault() == null) {
                addr.setIsDefault(0);
            }
            // 如果设为默认 先清除其他默认
            if (addr.getIsDefault() == 1) {
                clearDefault(addr.getUserId());
            }
            userAddressMapper.insert(addr);
        } else {
            if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
                clearDefault(addr.getUserId());
            }
            userAddressMapper.updateById(addr);
        }
        return Result.success(addr);
    }

    /**
     * 设为默认地址
     */
    @PostMapping("/setDefault/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        UserAddress addr = userAddressMapper.selectById(id);
        if (addr == null) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(addr.getUserId());
        addr.setIsDefault(1);
        userAddressMapper.updateById(addr);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userAddressMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default/{userId}")
    public Result<UserAddress> getDefault(@PathVariable Long userId) {
        UserAddress addr = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1));
        return Result.success(addr);
    }

    private void clearDefault(Long userId) {
        List<UserAddress> all = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId).eq(UserAddress::getIsDefault, 1));
        for (UserAddress a : all) {
            a.setIsDefault(0);
            userAddressMapper.updateById(a);
        }
    }
}
