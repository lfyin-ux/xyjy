package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.PersonalAuth;
import com.xyjy.entity.PersonalVerifyDaily;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.PersonalAuthMapper;
import com.xyjy.mapper.PersonalVerifyDailyMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;

@Service
public class PersonalAuthService {

    private static final int DAILY_FACE_VERIFY_LIMIT = 3;

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private PersonalAuthMapper personalAuthMapper;
    @Resource
    private PersonalVerifyDailyMapper personalVerifyDailyMapper;
    @Resource
    private GracePublishService gracePublishService;

    public void ensureNotVerified(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user != null && user.getIdentityVerified() != null && user.getIdentityVerified() == 1) {
            throw new BusinessException("您已完成个人认证，无需重复认证");
        }
    }

    /**
     * 每人每天允许发起有限次人脸核身（含失败），防止恶意刷接口
     */
    public void reserveDailyFaceVerify(Long userId) {
        LocalDate today = LocalDate.now();
        Long count = personalVerifyDailyMapper.selectCount(new LambdaQueryWrapper<PersonalVerifyDaily>()
                .eq(PersonalVerifyDaily::getUserId, userId)
                .eq(PersonalVerifyDaily::getVerifyDate, today));
        if (count != null && count >= DAILY_FACE_VERIFY_LIMIT) {
            throw new BusinessException("今日人脸核身次数已用完（每日" + DAILY_FACE_VERIFY_LIMIT + "次），请明天再试");
        }
        PersonalVerifyDaily record = new PersonalVerifyDaily();
        record.setUserId(userId);
        record.setVerifyDate(today);
        personalVerifyDailyMapper.insert(record);
    }

    /**
     * 提交个人认证，进入人工审核
     */
    public void submitForReview(PersonalAuth auth) {
        auth.setStatus(1);
        auth.setRejectReason(null);
        auth.setFaceVerified(0);
        personalAuthMapper.insert(auth);
    }

    /**
     * 管理后台审核通过
     */
    public void approvePersonalAuth(PersonalAuth auth) {
        auth.setStatus(2);
        auth.setRejectReason(null);
        personalAuthMapper.updateById(auth);
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setIdentityVerified(1);
            user.setPhone(auth.getPhone());
            appUserMapper.updateById(user);
            gracePublishService.onPersonalApproved(user);
        }
    }

    public void rejectPersonalAuth(PersonalAuth auth, String reason) {
        auth.setStatus(3);
        auth.setRejectReason(reason);
        personalAuthMapper.updateById(auth);
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setIdentityVerified(0);
            appUserMapper.updateById(user);
        }
    }

}
