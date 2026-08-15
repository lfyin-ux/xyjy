package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.UserBlacklist;
import com.xyjy.entity.UserFeedback;
import com.xyjy.entity.UserPhoto;
import com.xyjy.entity.UserVisit;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.UserBlacklistMapper;
import com.xyjy.mapper.UserFeedbackMapper;
import com.xyjy.mapper.UserPhotoMapper;
import com.xyjy.mapper.UserVisitMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 个人信息与个人中心接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private UserPhotoMapper userPhotoMapper;
    @Resource
    private UserVisitMapper userVisitMapper;
    @Resource
    private UserBlacklistMapper userBlacklistMapper;
    @Resource
    private UserFeedbackMapper userFeedbackMapper;

    /**
     * 查看用户详情 记录访客
     */
    @GetMapping("/detail/{id}")
    public Result<AppUser> detail(@PathVariable Long id,
                                  @RequestParam(required = false) Long visitorId) {
        AppUser user = appUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 记录访客
        if (visitorId != null && !visitorId.equals(id)) {
            UserVisit visit = new UserVisit();
            visit.setUserId(visitorId);
            visit.setTargetId(id);
            userVisitMapper.insert(visit);
        }
        return Result.success(user);
    }

    /**
     * 更新个人资料
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody AppUser user) {
        if (user.getId() == null) {
            throw new BusinessException("缺少用户ID");
        }
        // 修改已展示资料后头像和简介重新进入审核
        AppUser old = appUserMapper.selectById(user.getId());
        if (old != null) {
            if (user.getAvatar() != null && !user.getAvatar().equals(old.getAvatar())) {
                user.setAvatarAuditStatus(0);
            }
            if (user.getIntro() != null && !user.getIntro().equals(old.getIntro())) {
                user.setIntroAuditStatus(0);
            }
        }
        appUserMapper.updateById(user);
        return Result.success();
    }

    /**
     * 我的相册列表
     */
    @GetMapping("/photos/{userId}")
    public Result<List<UserPhoto>> photos(@PathVariable Long userId) {
        LambdaQueryWrapper<UserPhoto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhoto::getUserId, userId).orderByAsc(UserPhoto::getSort);
        return Result.success(userPhotoMapper.selectList(wrapper));
    }

    /**
     * 上传相册照片 需审核后展示
     */
    @PostMapping("/photo/add")
    public Result<Void> addPhoto(@RequestBody UserPhoto photo) {
        photo.setAuditStatus(0);
        userPhotoMapper.insert(photo);
        return Result.success();
    }

    /**
     * 删除相册照片
     */
    @DeleteMapping("/photo/{id}")
    public Result<Void> deletePhoto(@PathVariable Long id) {
        userPhotoMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 我的访客列表
     */
    @GetMapping("/visitors/{userId}")
    public Result<List<UserVisit>> visitors(@PathVariable Long userId) {
        LambdaQueryWrapper<UserVisit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVisit::getTargetId, userId).orderByDesc(UserVisit::getCreateTime);
        return Result.success(userVisitMapper.selectList(wrapper));
    }

    /**
     * 黑名单列表
     */
    @GetMapping("/blacklist/{userId}")
    public Result<List<UserBlacklist>> blacklist(@PathVariable Long userId) {
        LambdaQueryWrapper<UserBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlacklist::getUserId, userId);
        return Result.success(userBlacklistMapper.selectList(wrapper));
    }

    /**
     * 加入黑名单
     */
    @PostMapping("/blacklist/add")
    public Result<Void> addBlacklist(@RequestParam Long userId, @RequestParam Long targetId) {
        UserBlacklist b = new UserBlacklist();
        b.setUserId(userId);
        b.setTargetId(targetId);
        userBlacklistMapper.insert(b);
        return Result.success();
    }

    /**
     * 移出黑名单
     */
    @DeleteMapping("/blacklist")
    public Result<Void> removeBlacklist(@RequestParam Long userId, @RequestParam Long targetId) {
        userBlacklistMapper.delete(new LambdaQueryWrapper<UserBlacklist>()
                .eq(UserBlacklist::getUserId, userId).eq(UserBlacklist::getTargetId, targetId));
        return Result.success();
    }

    /**
     * 提交意见反馈
     */
    @PostMapping("/feedback")
    public Result<Void> feedback(@RequestBody UserFeedback feedback) {
        if (feedback.getContent() == null || feedback.getContent().isEmpty()) {
            throw new BusinessException("请填写反馈内容");
        }
        feedback.setStatus(0);
        userFeedbackMapper.insert(feedback);
        return Result.success();
    }
}
