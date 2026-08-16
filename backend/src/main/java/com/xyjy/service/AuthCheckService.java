package com.xyjy.service;

import com.xyjy.common.BusinessException;
import com.xyjy.entity.AppUser;
import com.xyjy.mapper.AppUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 认证状态校验服务 未双认证用户禁止使用核心功能
 */
@Service
public class AuthCheckService {

    @Resource
    private AppUserMapper appUserMapper;

    /**
     * 校验用户是否完成双认证 未通过则抛出异常
     */
    public void requireFullAuth(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getIdentityVerified() == null || user.getIdentityVerified() != 1
                || user.getSchoolVerified() == null || user.getSchoolVerified() != 1) {
            throw new BusinessException("请先完成个人认证和学校认证后才能使用该功能");
        }
    }

    /**
     * 判断用户是否已完成双认证
     */
    public boolean isFullAuth(Long userId) {
        if (userId == null) return false;
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) return false;
        return user.getIdentityVerified() != null && user.getIdentityVerified() == 1
                && user.getSchoolVerified() != null && user.getSchoolVerified() == 1;
    }
}
