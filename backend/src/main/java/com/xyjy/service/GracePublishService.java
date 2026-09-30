package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.SchoolAuth;
import com.xyjy.entity.SchoolInfo;
import com.xyjy.entity.SquarePost;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.SchoolAuthMapper;
import com.xyjy.mapper.SquarePostMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 个人认证通过后、学籍认证完成前的宽限发布（1 条动态，2 小时内须完成学籍认证）
 */
@Service
public class GracePublishService {

    public static final int GRACE_HOURS = 2;

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private SquarePostMapper squarePostMapper;
    @Resource
    private SchoolAuthMapper schoolAuthMapper;
    @Resource
    private SchoolInfoService schoolInfoService;

    public void refreshGraceState(Long userId) {
        if (userId == null) {
            return;
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        if (isSchoolVerified(user)) {
            return;
        }
        if (user.getPersonalApprovedAt() == null) {
            return;
        }
        if (!isGraceExpired(user)) {
            return;
        }
        revokeGracePublish(user);
    }

    public boolean isSchoolVerified(AppUser user) {
        return user != null && user.getSchoolVerified() != null && user.getSchoolVerified() == 1;
    }

    public boolean isGraceExpired(AppUser user) {
        if (user == null || user.getPersonalApprovedAt() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(user.getPersonalApprovedAt().plusHours(GRACE_HOURS));
    }

    public boolean canGracePublish(AppUser user) {
        if (user == null || user.getIdentityVerified() == null || user.getIdentityVerified() != 1) {
            return false;
        }
        if (isSchoolVerified(user)) {
            return false;
        }
        if (user.getPersonalApprovedAt() == null) {
            return false;
        }
        if (isGraceExpired(user)) {
            return false;
        }
        return user.getGracePublishUsed() == null || user.getGracePublishUsed() == 0;
    }

    public boolean canPublishSquare(Long userId) {
        refreshGraceState(userId);
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if (isSchoolVerified(user)) {
            return true;
        }
        return canGracePublish(user);
    }

    public void assertCanPublishSquare(Long userId) {
        refreshGraceState(userId);
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (isSchoolVerified(user)) {
            return;
        }
        if (user.getIdentityVerified() == null || user.getIdentityVerified() != 1) {
            throw new BusinessException("请先完成并通过个人认证");
        }
        if (user.getPersonalApprovedAt() == null) {
            throw new BusinessException("个人认证审核中，请耐心等待");
        }
        if (isGraceExpired(user)) {
            throw new BusinessException("已超过学籍认证宽限时间，请先完成学校认证后再发布");
        }
        if (user.getGracePublishUsed() != null && user.getGracePublishUsed() == 1) {
            throw new BusinessException("宽限期内仅可发布 1 条动态，请先完成学校认证");
        }
    }

    public Long resolveSchoolIdForGrace(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getSchoolId() != null) {
            return user.getSchoolId();
        }
        if (user.getCurrentSchoolId() != null) {
            return user.getCurrentSchoolId();
        }
        SchoolAuth sa = schoolAuthMapper.selectOne(new LambdaQueryWrapper<SchoolAuth>()
                .eq(SchoolAuth::getUserId, userId)
                .orderByDesc(SchoolAuth::getId)
                .last("limit 1"));
        if (sa != null) {
            if (sa.getSchoolId() != null) {
                return sa.getSchoolId();
            }
            if (sa.getSchoolName() != null && !sa.getSchoolName().isEmpty()) {
                SchoolInfo school = schoolInfoService.findOrCreate(sa.getSchoolName());
                return school.getId();
            }
        }
        if (user.getSchool() != null && !user.getSchool().isEmpty()) {
            SchoolInfo school = schoolInfoService.findOrCreate(user.getSchool());
            return school.getId();
        }
        throw new BusinessException("请先提交学校认证或完善学校信息后再发布");
    }

    public void markGracePostUsed(Long userId, Long postId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null || isSchoolVerified(user)) {
            return;
        }
        user.setGracePublishUsed(1);
        user.setGracePostId(postId);
        appUserMapper.updateById(user);
    }

    public void onPersonalApproved(AppUser user) {
        if (user == null) {
            return;
        }
        user.setPersonalApprovedAt(LocalDateTime.now());
        user.setGracePublishUsed(0);
        user.setGracePostId(null);
        appUserMapper.updateById(user);
    }

    public void onSchoolApproved(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        user.setGracePublishUsed(0);
        user.setGracePostId(null);
        appUserMapper.updateById(user);
    }

    private void revokeGracePublish(AppUser user) {
        if (user.getGracePostId() != null) {
            SquarePost post = squarePostMapper.selectById(user.getGracePostId());
            if (post != null && post.getStatus() != null && post.getStatus() != 6) {
                post.setStatus(6);
                squarePostMapper.updateById(post);
            }
        }
        user.setGracePublishUsed(1);
        appUserMapper.updateById(user);
    }

    public Map<String, Object> graceInfo(Long userId) {
        refreshGraceState(userId);
        Map<String, Object> map = new HashMap<>();
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            map.put("canPublishPost", false);
            return map;
        }
        boolean full = isSchoolVerified(user);
        boolean grace = canGracePublish(user);
        map.put("canPublishPost", full || grace);
        map.put("gracePublishUsed", user.getGracePublishUsed() != null && user.getGracePublishUsed() == 1);
        if (user.getPersonalApprovedAt() != null && !full) {
            map.put("graceDeadline", user.getPersonalApprovedAt().plusHours(GRACE_HOURS).toString().replace('T', ' '));
        }
        return map;
    }
}
