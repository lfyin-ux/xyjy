package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.SchoolInfo;
import com.xyjy.entity.UserBlacklist;
import com.xyjy.entity.UserFeedback;
import com.xyjy.entity.UserFollow;
import com.xyjy.entity.UserPhoto;
import com.xyjy.entity.UserVisit;
import com.xyjy.entity.ViolationRecord;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.SchoolInfoMapper;
import com.xyjy.service.MallSpendService;
import com.xyjy.mapper.UserBlacklistMapper;
import com.xyjy.mapper.UserFeedbackMapper;
import com.xyjy.mapper.UserFollowMapper;
import com.xyjy.mapper.UserPhotoMapper;
import com.xyjy.mapper.UserVisitMapper;
import com.xyjy.mapper.ViolationRecordMapper;
import com.xyjy.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private FilterService filterService;
    @Resource
    private UserBlacklistMapper userBlacklistMapper;
    @Resource
    private UserFeedbackMapper userFeedbackMapper;
    @Resource
    private com.xyjy.service.BlacklistService blacklistService;
    @Resource
    private com.xyjy.service.SchoolScopeService schoolScopeService;
    @Resource
    private MallSpendService mallSpendService;
    @Resource
    private com.xyjy.service.SchoolInfoService schoolInfoService;
    @Resource
    private SchoolInfoMapper schoolInfoMapper;
    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private ViolationRecordMapper violationRecordMapper;

    /**
     * 学校浏览上下文：当前学校、是否可写、消费解锁进度
     */
    @GetMapping("/school/context")
    public Result<Map<String, Object>> schoolContext(@RequestParam Long userId) {
        AppUser user = schoolScopeService.requireVerifiedUser(userId);
        mallSpendService.refreshUserSpend(userId);
        user = appUserMapper.selectById(userId);
        Long homeId = user.getSchoolId();
        Long currentId = user.getCurrentSchoolId() != null ? user.getCurrentSchoolId() : homeId;
        SchoolInfo home = homeId != null ? schoolInfoMapper.selectById(homeId) : null;
        SchoolInfo current = currentId != null ? schoolInfoMapper.selectById(currentId) : null;
        boolean isHome = homeId != null && homeId.equals(currentId);
        boolean canWrite = schoolScopeService.canWriteInViewSchool(userId);
        BigDecimal spent = user.getMallTotalSpent() != null ? user.getMallTotalSpent() : BigDecimal.ZERO;
        Map<String, Object> map = new HashMap<>();
        map.put("homeSchoolId", homeId);
        map.put("homeSchoolName", home != null ? home.getSchoolName() : user.getSchool());
        map.put("currentSchoolId", currentId);
        map.put("currentSchoolName", current != null ? current.getSchoolName() : user.getSchool());
        map.put("isHomeSchool", isHome);
        map.put("canWrite", canWrite);
        map.put("viewOnly", !canWrite);
        map.put("mallTotalSpent", spent);
        map.put("unlockAmount", MallSpendService.CROSS_SCHOOL_UNLOCK_AMOUNT);
        map.put("crossSchoolUnlocked", mallSpendService.isCrossSchoolUnlocked(user));
        map.put("unlockRemain", schoolScopeService.getUnlockRemain(user));
        return Result.success(map);
    }

    /**
     * 切换当前浏览学校
     */
    @PostMapping("/school/switch")
    public Result<Map<String, Object>> switchSchool(@RequestParam Long userId,
                                                    @RequestParam Long schoolId) {
        schoolScopeService.requireVerifiedUser(userId);
        SchoolInfo school = schoolInfoService.requireById(schoolId);
        AppUser user = appUserMapper.selectById(userId);
        user.setCurrentSchoolId(school.getId());
        appUserMapper.updateById(user);
        return schoolContext(userId);
    }

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
        // 黑名单校验 访客与目标存在拉黑关系时不可查看 黑名单双向生效
        if (visitorId != null && !visitorId.equals(id) && blacklistService.hasBlock(visitorId, id)) {
            throw new BusinessException("因黑名单关系，无法查看该用户");
        }
        schoolScopeService.assertCanViewUser(visitorId, id);
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
        String profileText = (user.getNickname() == null ? "" : user.getNickname())
                + (user.getIntro() == null ? "" : user.getIntro());
        FilterService.FilterResult fr = filterService.check(profileText, "个人资料", user.getId());
        if (fr.level == 2) {
            throw new BusinessException(fr.tip);
        }
        // 头像、简介上传后直接生效
        user.setAvatarAuditStatus(1);
        user.setIntroAuditStatus(1);
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
     * 我的关注列表
     */
    @GetMapping("/follow/list/{userId}")
    public Result<List<AppUser>> followList(@PathVariable Long userId) {
        List<UserFollow> follows = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .orderByDesc(UserFollow::getCreateTime));
        if (follows.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Long> ids = follows.stream().map(UserFollow::getTargetId).collect(Collectors.toList());
        Map<Long, AppUser> userMap = appUserMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(AppUser::getId, u -> u, (a, b) -> a));
        List<AppUser> ordered = ids.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .filter(u -> !blacklistService.hasBlock(userId, u.getId()))
                .collect(Collectors.toList());
        return Result.success(ordered);
    }

    /**
     * 查询是否已关注
     */
    @GetMapping("/follow/status")
    public Result<Map<String, Object>> followStatus(@RequestParam Long userId,
                                                    @RequestParam Long targetId) {
        long count = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getTargetId, targetId));
        Map<String, Object> map = new HashMap<>();
        map.put("followed", count > 0);
        return Result.success(map);
    }

    /**
     * 关注用户
     */
    @PostMapping("/follow/add")
    public Result<Void> addFollow(@RequestParam Long userId, @RequestParam Long targetId) {
        if (userId.equals(targetId)) {
            throw new BusinessException("不能关注自己");
        }
        AppUser target = appUserMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        if (blacklistService.hasBlock(userId, targetId)) {
            throw new BusinessException("因黑名单关系，无法关注该用户");
        }
        schoolScopeService.assertCanViewUser(userId, targetId);
        Long exists = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getTargetId, targetId));
        if (exists > 0) {
            return Result.success();
        }
        UserFollow follow = new UserFollow();
        follow.setUserId(userId);
        follow.setTargetId(targetId);
        userFollowMapper.insert(follow);
        return Result.success();
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/follow")
    public Result<Void> removeFollow(@RequestParam Long userId, @RequestParam Long targetId) {
        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getTargetId, targetId));
        return Result.success();
    }

    /**
     * 我的平台通知（违规/警告记录）
     */
    @GetMapping("/violations/{userId}")
    public Result<List<ViolationRecord>> violations(@PathVariable Long userId) {
        List<ViolationRecord> list = violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, userId)
                .orderByDesc(ViolationRecord::getCreateTime));
        return Result.success(list);
    }

    /**
     * 未读平台通知数量
     */
    @GetMapping("/violations/unread-count/{userId}")
    public Result<Map<String, Object>> violationUnreadCount(@PathVariable Long userId) {
        long count = violationRecordMapper.selectCount(new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, userId)
                .and(w -> w.isNull(ViolationRecord::getReadStatus).or().eq(ViolationRecord::getReadStatus, 0)));
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        return Result.success(map);
    }

    /**
     * 标记平台通知已读
     */
    @PostMapping("/violations/read")
    public Result<Void> readViolation(@RequestParam Long userId, @RequestParam Long id) {
        ViolationRecord record = violationRecordMapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException("通知不存在");
        }
        record.setReadStatus(1);
        violationRecordMapper.updateById(record);
        return Result.success();
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/violations/read-all")
    public Result<Void> readAllViolations(@RequestParam Long userId) {
        ViolationRecord update = new ViolationRecord();
        update.setReadStatus(1);
        violationRecordMapper.update(update, new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, userId)
                .and(w -> w.isNull(ViolationRecord::getReadStatus).or().eq(ViolationRecord::getReadStatus, 0)));
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
        FilterService.FilterResult fr = filterService.check(feedback.getContent(), "意见反馈", feedback.getUserId());
        if (fr.level == 2) {
            throw new BusinessException(fr.tip);
        }
        feedback.setStatus(0);
        userFeedbackMapper.insert(feedback);
        return Result.success();
    }
}
