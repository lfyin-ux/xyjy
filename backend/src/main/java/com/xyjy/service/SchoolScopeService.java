package com.xyjy.service;

import com.xyjy.common.BusinessException;
import com.xyjy.entity.AppUser;
import com.xyjy.mapper.AppUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 同校隔离：浏览学校切换 + 跨校写入权限
 */
@Service
public class SchoolScopeService {

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private MallSpendService mallSpendService;

    /**
     * 列表查询用：当前浏览的学校ID
     */
    public Long resolveListSchoolId(Long userId) {
        return resolveViewSchoolId(userId);
    }

    /**
     * 当前浏览学校（默认认证学校）
     */
    public Long resolveViewSchoolId(Long userId) {
        if (userId == null) {
            return null;
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null || !isSchoolVerified(user)) {
            return null;
        }
        if (user.getCurrentSchoolId() != null) {
            return user.getCurrentSchoolId();
        }
        return user.getSchoolId();
    }

    public Long getHomeSchoolId(AppUser user) {
        return user == null ? null : user.getSchoolId();
    }

    public boolean isViewingHomeSchool(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null || user.getSchoolId() == null) {
            return true;
        }
        Long current = user.getCurrentSchoolId() != null ? user.getCurrentSchoolId() : user.getSchoolId();
        return user.getSchoolId().equals(current);
    }

    public boolean canWriteInViewSchool(Long userId) {
        AppUser user = requireVerifiedUser(userId);
        if (isViewingHomeSchool(userId)) {
            return true;
        }
        return mallSpendService.isCrossSchoolUnlocked(user);
    }

    /**
     * 发布/互动用：当前浏览学校且具备写入权限
     */
    public Long requireViewSchoolIdForWrite(Long userId) {
        Long schoolId = resolveViewSchoolId(userId);
        if (schoolId == null) {
            throw new BusinessException("请先完成学校认证");
        }
        assertCanWrite(userId, schoolId);
        return schoolId;
    }

    /** @deprecated 使用 requireViewSchoolIdForWrite */
    public Long requireSchoolId(Long userId) {
        return requireViewSchoolIdForWrite(userId);
    }

    /**
     * 读取：内容须属于当前浏览学校
     */
    public void assertCanAccess(Long viewerId, Long resourceSchoolId) {
        if (resourceSchoolId == null) {
            throw new BusinessException("内容不存在或无权查看");
        }
        Long viewSchoolId = resolveViewSchoolId(viewerId);
        if (viewSchoolId == null) {
            throw new BusinessException("请先完成学校认证");
        }
        if (!viewSchoolId.equals(resourceSchoolId)) {
            throw new BusinessException("请切换到对应学校后查看");
        }
    }

    /**
     * 写入：在当前浏览学校且非本校时需满消费门槛
     */
    public void assertCanWrite(Long userId, Long resourceSchoolId) {
        requireVerifiedUser(userId);
        assertCanAccess(userId, resourceSchoolId);
        if (!canWriteInViewSchool(userId)) {
            throw new BusinessException("当前学校为浏览模式，在本校商城累计消费满2000元后可在外校互动");
        }
    }

    /**
     * 匹配/聊天：仅认证所属学校内互动
     */
    public void assertSameSchool(Long userId, Long targetUserId) {
        assertSameHomeSchool(userId, targetUserId);
    }

    public void assertSameHomeSchool(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            throw new BusinessException("参数缺失");
        }
        if (userId.equals(targetUserId)) {
            return;
        }
        AppUser me = appUserMapper.selectById(userId);
        AppUser target = appUserMapper.selectById(targetUserId);
        if (me == null || target == null) {
            throw new BusinessException("用户不存在");
        }
        if (!isSchoolVerified(me) || !isSchoolVerified(target)) {
            throw new BusinessException("请先完成学校认证");
        }
        if (me.getSchoolId() == null || target.getSchoolId() == null
                || !me.getSchoolId().equals(target.getSchoolId())) {
            throw new BusinessException("只能与同一认证学校的用户互动");
        }
    }

    /**
     * 查看他人资料：须在当前浏览学校
     */
    public void assertCanViewUser(Long visitorId, Long targetUserId) {
        if (visitorId == null || visitorId.equals(targetUserId)) {
            return;
        }
        AppUser target = appUserMapper.selectById(targetUserId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        Long viewSchoolId = resolveViewSchoolId(visitorId);
        if (viewSchoolId == null) {
            throw new BusinessException("请先完成学校认证");
        }
        if (target.getSchoolId() == null || !target.getSchoolId().equals(viewSchoolId)) {
            throw new BusinessException("只能查看当前浏览学校的用户");
        }
    }

    public AppUser requireVerifiedUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!isSchoolVerified(user) || user.getSchoolId() == null) {
            throw new BusinessException("请先完成学校认证");
        }
        return user;
    }

    public BigDecimal getUnlockRemain(AppUser user) {
        BigDecimal spent = user.getMallTotalSpent() != null ? user.getMallTotalSpent() : BigDecimal.ZERO;
        BigDecimal remain = MallSpendService.CROSS_SCHOOL_UNLOCK_AMOUNT.subtract(spent);
        return remain.compareTo(BigDecimal.ZERO) > 0 ? remain : BigDecimal.ZERO;
    }

    private boolean isSchoolVerified(AppUser user) {
        return user.getSchoolVerified() != null && user.getSchoolVerified() == 1;
    }

    /**
     * 校园生活列表：登录即可浏览，按当前浏览/绑定学校筛选（未绑定则看全部）
     */
    public Long resolveCampusLifeListSchoolId(Long userId) {
        if (userId == null) {
            return null;
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        if (user.getCurrentSchoolId() != null) {
            return user.getCurrentSchoolId();
        }
        return user.getSchoolId();
    }

    /**
     * 校园生活发布：登录即可，不强制学校认证与跨校消费门槛
     */
    public Long requireCampusLifeSchoolIdForWrite(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getCurrentSchoolId() != null) {
            return user.getCurrentSchoolId();
        }
        return user.getSchoolId();
    }

    /**
     * 校园生活详情：有学校上下文时同校可见，否则均可浏览
     */
    public void assertCampusLifeAccess(Long viewerId, Long resourceSchoolId) {
        if (resourceSchoolId == null || viewerId == null) {
            return;
        }
        Long viewSchoolId = resolveCampusLifeListSchoolId(viewerId);
        if (viewSchoolId == null) {
            return;
        }
        if (!viewSchoolId.equals(resourceSchoolId)) {
            throw new BusinessException("请切换到对应学校后查看");
        }
    }
}
