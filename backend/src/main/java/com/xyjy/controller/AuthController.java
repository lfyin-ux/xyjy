package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.PersonalAuth;
import com.xyjy.entity.SchoolAuth;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.PersonalAuthMapper;
import com.xyjy.mapper.SchoolAuthMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端 登录与认证接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private PersonalAuthMapper personalAuthMapper;
    @Resource
    private SchoolAuthMapper schoolAuthMapper;

    /**
     * 微信一键登录 演示用openid直接登录或注册
     */
    @PostMapping("/wxLogin")
    public Result<AppUser> wxLogin(@RequestParam String openid,
                                   @RequestParam(required = false) String nickname) {
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getOpenid, openid);
        AppUser user = appUserMapper.selectOne(wrapper);
        if (user == null) {
            // 首次登录自动创建账号
            user = new AppUser();
            user.setOpenid(openid);
            user.setNickname(nickname != null ? nickname : "新同学");
            user.setStatus(1);
            user.setIdentityVerified(0);
            user.setSchoolVerified(0);
            appUserMapper.insert(user);
        }
        return Result.success(user);
    }

    /**
     * 发送短信验证码 演示固定返回123456
     */
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestParam String phone) {
        if (!phone.matches("^1\\d{10}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        return Result.success("123456");
    }

    /**
     * 查询用户认证状态汇总
     */
    @GetMapping("/status/{userId}")
    public Result<Map<String, Object>> authStatus(@PathVariable Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("identityVerified", user.getIdentityVerified());
        map.put("schoolVerified", user.getSchoolVerified());
        map.put("fullAccess", user.getIdentityVerified() != null && user.getIdentityVerified() == 1
                && user.getSchoolVerified() != null && user.getSchoolVerified() == 1);
        // 查询认证详情状态
        PersonalAuth pa = personalAuthMapper.selectOne(new LambdaQueryWrapper<PersonalAuth>()
                .eq(PersonalAuth::getUserId, userId).orderByDesc(PersonalAuth::getId).last("limit 1"));
        SchoolAuth sa = schoolAuthMapper.selectOne(new LambdaQueryWrapper<SchoolAuth>()
                .eq(SchoolAuth::getUserId, userId).orderByDesc(SchoolAuth::getId).last("limit 1"));
        map.put("personalStatus", pa != null ? pa.getStatus() : 0);
        map.put("personalReject", pa != null ? pa.getRejectReason() : null);
        map.put("schoolStatus", sa != null ? sa.getStatus() : 0);
        map.put("schoolReject", sa != null ? sa.getRejectReason() : null);
        return Result.success(map);
    }

    /**
     * 提交个人认证
     */
    @PostMapping("/personal/submit")
    public Result<Void> submitPersonal(@RequestBody PersonalAuth auth) {
        if (auth.getUserId() == null) {
            throw new BusinessException("缺少用户ID");
        }
        if (auth.getPhone() == null || !auth.getPhone().matches("^1\\d{10}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        if (auth.getRealName() == null || auth.getRealName().isEmpty()) {
            throw new BusinessException("请填写真实姓名");
        }
        if (auth.getIdCard() == null || !auth.getIdCard().matches("^[1-9]\\d{16}[\\dXx]$")) {
            throw new BusinessException("身份证号码格式不正确");
        }
        if (auth.getIdFrontImg() == null || auth.getIdBackImg() == null) {
            throw new BusinessException("请上传身份证正反面照片");
        }
        // 提交后进入审核中 覆盖旧记录
        personalAuthMapper.delete(new LambdaQueryWrapper<PersonalAuth>().eq(PersonalAuth::getUserId, auth.getUserId()));
        auth.setStatus(1);
        auth.setRejectReason(null);
        personalAuthMapper.insert(auth);
        return Result.success();
    }

    /**
     * 提交学校认证 必须个人认证通过后才能提交
     */
    @PostMapping("/school/submit")
    public Result<Void> submitSchool(@RequestBody SchoolAuth auth) {
        if (auth.getUserId() == null) {
            throw new BusinessException("缺少用户ID");
        }
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user == null || user.getIdentityVerified() == null || user.getIdentityVerified() != 1) {
            throw new BusinessException("请先完成并通过个人认证");
        }
        if (auth.getSchoolName() == null || auth.getSchoolName().isEmpty()) {
            throw new BusinessException("请填写学校名称");
        }
        if (auth.getDocImgs() == null || auth.getDocImgs().isEmpty()) {
            throw new BusinessException("请上传学校证明材料");
        }
        schoolAuthMapper.delete(new LambdaQueryWrapper<SchoolAuth>().eq(SchoolAuth::getUserId, auth.getUserId()));
        auth.setStatus(1);
        auth.setRejectReason(null);
        schoolAuthMapper.insert(auth);
        return Result.success();
    }
}
