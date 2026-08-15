package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.*;
import com.xyjy.mapper.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 用户管理接口
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private UserPhotoMapper userPhotoMapper;
    @Resource
    private PersonalAuthMapper personalAuthMapper;
    @Resource
    private SchoolAuthMapper schoolAuthMapper;
    @Resource
    private ViolationRecordMapper violationRecordMapper;

    /**
     * 用户列表 支持多条件搜索
     */
    @GetMapping("/list")
    public Result<Page<AppUser>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String school,
                                      @RequestParam(required = false) Integer status) {
        Page<AppUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(AppUser::getNickname, keyword)
                    .or().like(AppUser::getPhone, keyword)
                    .or().eq(AppUser::getId, isNumber(keyword) ? Long.valueOf(keyword) : -1L));
        }
        if (school != null && !school.isEmpty()) {
            wrapper.eq(AppUser::getSchool, school);
        }
        if (status != null) {
            wrapper.eq(AppUser::getStatus, status);
        }
        wrapper.orderByDesc(AppUser::getCreateTime);
        return Result.success(appUserMapper.selectPage(page, wrapper));
    }

    private boolean isNumber(String s) {
        return s != null && s.matches("\\d+");
    }

    /**
     * 用户详情 含相册和认证信息
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        AppUser user = appUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("photos", userPhotoMapper.selectList(new LambdaQueryWrapper<UserPhoto>()
                .eq(UserPhoto::getUserId, id)));
        map.put("personalAuth", personalAuthMapper.selectOne(new LambdaQueryWrapper<PersonalAuth>()
                .eq(PersonalAuth::getUserId, id).orderByDesc(PersonalAuth::getId).last("limit 1")));
        map.put("schoolAuth", schoolAuthMapper.selectOne(new LambdaQueryWrapper<SchoolAuth>()
                .eq(SchoolAuth::getUserId, id).orderByDesc(SchoolAuth::getId).last("limit 1")));
        map.put("violations", violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, id)));
        return Result.success(map);
    }

    /**
     * 审核用户资料 头像或简介 type: avatar/intro pass: true通过
     */
    @PostMapping("/auditProfile")
    public Result<Void> auditProfile(@RequestParam Long userId, @RequestParam String type,
                                     @RequestParam Boolean pass) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        int st = pass ? 1 : 2;
        if ("avatar".equals(type)) {
            user.setAvatarAuditStatus(st);
        } else if ("intro".equals(type)) {
            user.setIntroAuditStatus(st);
        }
        appUserMapper.updateById(user);
        return Result.success();
    }

    /**
     * 审核相册照片
     */
    @PostMapping("/auditPhoto")
    public Result<Void> auditPhoto(@RequestParam Long photoId, @RequestParam Boolean pass) {
        UserPhoto photo = userPhotoMapper.selectById(photoId);
        if (photo == null) {
            throw new BusinessException("照片不存在");
        }
        photo.setAuditStatus(pass ? 1 : 2);
        userPhotoMapper.updateById(photo);
        return Result.success();
    }

    /**
     * 限制发言
     */
    @PostMapping("/limitSpeak")
    public Result<Void> limitSpeak(@RequestParam Long userId, @RequestParam(required = false) String reason) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(2);
        user.setSpeakLimitEnd(LocalDateTime.now().plusDays(7));
        appUserMapper.updateById(user);
        addViolation(userId, "limit", reason);
        return Result.success();
    }

    /**
     * 封禁账号
     */
    @PostMapping("/ban")
    public Result<Void> ban(@RequestParam Long userId, @RequestParam(required = false) String reason) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(3);
        appUserMapper.updateById(user);
        addViolation(userId, "ban", reason);
        return Result.success();
    }

    /**
     * 解冻解封账号
     */
    @PostMapping("/unban")
    public Result<Void> unban(@RequestParam Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(1);
        user.setSpeakLimitEnd(null);
        appUserMapper.updateById(user);
        return Result.success();
    }

    /**
     * 警告用户
     */
    @PostMapping("/warn")
    public Result<Void> warn(@RequestParam Long userId, @RequestParam(required = false) String reason) {
        addViolation(userId, "warn", reason);
        return Result.success();
    }

    private void addViolation(Long userId, String type, String reason) {
        ViolationRecord v = new ViolationRecord();
        v.setUserId(userId);
        v.setType(type);
        v.setReason(reason);
        v.setAdminName("管理员");
        violationRecordMapper.insert(v);
    }
}
