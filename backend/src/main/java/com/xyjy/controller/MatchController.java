package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.*;
import com.xyjy.mapper.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端 匹配推荐接口
 */
@RestController
@RequestMapping("/match")
public class MatchController {

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private UserLikeMapper userLikeMapper;
    @Resource
    private UserSkipMapper userSkipMapper;
    @Resource
    private UserMatchMapper userMatchMapper;
    @Resource
    private UserBlacklistMapper userBlacklistMapper;
    @Resource
    private ChatSessionMapper chatSessionMapper;
    @Resource
    private com.xyjy.service.AuthCheckService authCheckService;

    /**
     * 推荐列表 排除本人 已喜欢 已跳过 黑名单 封禁用户 支持筛选
     */
    @GetMapping("/recommend")
    public Result<List<AppUser>> recommend(@RequestParam Long userId,
                                           @RequestParam(required = false) Integer gender,
                                           @RequestParam(required = false) String tag,
                                           @RequestParam(required = false) String partnerType) {
        // 必须双认证才能使用匹配
        authCheckService.requireFullAuth(userId);
        AppUser me = appUserMapper.selectById(userId);
        if (me == null) {
            throw new BusinessException("用户不存在");
        }
        // 收集需排除的用户ID
        List<Long> excludeIds = new ArrayList<>();
        excludeIds.add(userId);
        userSkipMapper.selectList(new LambdaQueryWrapper<UserSkip>().eq(UserSkip::getUserId, userId))
                .forEach(s -> excludeIds.add(s.getTargetId()));
        userLikeMapper.selectList(new LambdaQueryWrapper<UserLike>().eq(UserLike::getUserId, userId))
                .forEach(l -> excludeIds.add(l.getTargetId()));
        // 我拉黑的人
        userBlacklistMapper.selectList(new LambdaQueryWrapper<UserBlacklist>().eq(UserBlacklist::getUserId, userId))
                .forEach(b -> excludeIds.add(b.getTargetId()));
        // 拉黑我的人 黑名单双向生效 对方也看不到我 我也看不到对方
        userBlacklistMapper.selectList(new LambdaQueryWrapper<UserBlacklist>().eq(UserBlacklist::getTargetId, userId))
                .forEach(b -> excludeIds.add(b.getUserId()));

        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getStatus, 1)
                .eq(AppUser::getIdentityVerified, 1)
                .eq(AppUser::getSchoolVerified, 1)
                .notIn(AppUser::getId, excludeIds);
        // 同校隔离
        if (me.getSchool() != null) {
            wrapper.eq(AppUser::getSchool, me.getSchool());
        }
        if (gender != null) {
            wrapper.eq(AppUser::getGender, gender);
        }
        if (tag != null && !tag.isEmpty()) {
            wrapper.like(AppUser::getTags, tag);
        }
        if (partnerType != null && !partnerType.isEmpty()) {
            wrapper.like(AppUser::getPartnerType, partnerType);
        }
        List<AppUser> list = appUserMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 喜欢 双向喜欢自动匹配并创建会话
     */
    @PostMapping("/like")
    public Result<Map<String, Object>> like(@RequestParam Long userId,
                                            @RequestParam Long targetId,
                                            @RequestParam(defaultValue = "1") Integer type) {
        // 记录喜欢
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setType(type);
        userLikeMapper.insert(like);

        Map<String, Object> result = new HashMap<>();
        result.put("matched", false);
        // 检查对方是否也喜欢我
        Long back = userLikeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, targetId).eq(UserLike::getTargetId, userId));
        if (back != null && back > 0) {
            // 双向喜欢 达成匹配
            Long exist = userMatchMapper.selectCount(new LambdaQueryWrapper<UserMatch>()
                    .and(w -> w.eq(UserMatch::getUserA, userId).eq(UserMatch::getUserB, targetId))
                    .or(w -> w.eq(UserMatch::getUserA, targetId).eq(UserMatch::getUserB, userId)));
            if (exist == null || exist == 0) {
                UserMatch match = new UserMatch();
                match.setUserA(userId);
                match.setUserB(targetId);
                match.setStatus(1);
                userMatchMapper.insert(match);
                // 创建聊天会话
                ChatSession session = new ChatSession();
                session.setUserA(userId);
                session.setUserB(targetId);
                session.setLocked(0);
                session.setStatus(1);
                chatSessionMapper.insert(session);
            }
            result.put("matched", true);
        }
        return Result.success(result);
    }

    /**
     * 跳过
     */
    @PostMapping("/skip")
    public Result<Void> skip(@RequestParam Long userId, @RequestParam Long targetId) {
        UserSkip skip = new UserSkip();
        skip.setUserId(userId);
        skip.setTargetId(targetId);
        userSkipMapper.insert(skip);
        return Result.success();
    }

    /**
     * 我的匹配列表
     */
    @GetMapping("/list/{userId}")
    public Result<List<AppUser>> matchList(@PathVariable Long userId) {
        List<UserMatch> matches = userMatchMapper.selectList(new LambdaQueryWrapper<UserMatch>()
                .eq(UserMatch::getStatus, 1)
                .and(w -> w.eq(UserMatch::getUserA, userId).or().eq(UserMatch::getUserB, userId)));
        List<Long> ids = matches.stream()
                .map(m -> m.getUserA().equals(userId) ? m.getUserB() : m.getUserA())
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(appUserMapper.selectBatchIds(ids));
    }

    /**
     * 取消匹配
     */
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestParam Long userId, @RequestParam Long targetId) {
        List<UserMatch> matches = userMatchMapper.selectList(new LambdaQueryWrapper<UserMatch>()
                .and(w -> w.eq(UserMatch::getUserA, userId).eq(UserMatch::getUserB, targetId))
                .or(w -> w.eq(UserMatch::getUserA, targetId).eq(UserMatch::getUserB, userId)));
        for (UserMatch m : matches) {
            m.setStatus(0);
            userMatchMapper.updateById(m);
        }
        return Result.success();
    }

    /**
     * 谁喜欢我
     */
    @GetMapping("/whoLikesMe/{userId}")
    public Result<List<AppUser>> whoLikesMe(@PathVariable Long userId) {
        List<UserLike> likes = userLikeMapper.selectList(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getTargetId, userId));
        List<Long> ids = likes.stream().map(UserLike::getUserId).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(appUserMapper.selectBatchIds(ids));
    }

    /**
     * 我的特别关注列表
     */
    @GetMapping("/myStars/{userId}")
    public Result<List<AppUser>> myStars(@PathVariable Long userId) {
        List<UserLike> stars = userLikeMapper.selectList(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId).eq(UserLike::getType, 2));
        List<Long> ids = stars.stream().map(UserLike::getTargetId).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(appUserMapper.selectBatchIds(ids));
    }
}
