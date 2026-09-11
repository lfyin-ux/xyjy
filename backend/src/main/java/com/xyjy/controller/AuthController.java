package com.xyjy.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.PersonalAuth;
import com.xyjy.entity.SchoolAuth;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.PersonalAuthMapper;
import com.xyjy.mapper.SchoolAuthMapper;
import com.xyjy.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端 登录与认证接口 支持开发模式和生产模式
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

    @Value("${app.mode:dev}")
    private String appMode;
    @Value("${wechat.app-id:}")
    private String appId;
    @Value("${wechat.app-secret:}")
    private String appSecret;
    @Resource
    private com.xyjy.service.SmsService smsService;

    /**
     * 获取当前应用模式 前端据此决定走哪种登录流程
     */
    @GetMapping("/mode")
    public Result<Map<String, Object>> getMode() {
        Map<String, Object> map = new HashMap<>();
        map.put("mode", appMode);
        return Result.success(map);
    }

    /**
     * 开发模式 获取所有mock用户列表 方便切换登录
     */
    @GetMapping("/devUsers")
    public Result<List<AppUser>> devUsers() {
        if (!"dev".equals(appMode)) {
            throw new BusinessException("仅开发模式可用");
        }
        return Result.success(appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                .orderByAsc(AppUser::getId)));
    }

    /**
     * 开发模式 直接用openid登录 用于调试多用户聊天等功能
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
     * 生产模式 微信授权登录 前端传wx.login()获取的code 后端换取openid
     */
    @PostMapping("/wxCodeLogin")
    public Result<AppUser> wxCodeLogin(@RequestParam String code) {
        if ("dev".equals(appMode)) {
            throw new BusinessException("生产模式接口，当前为开发模式请使用wxLogin");
        }
        // 调用微信接口用code换取openid和session_key
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + appId
                + "&secret=" + appSecret
                + "&js_code=" + code
                + "&grant_type=authorization_code";
        String resp;
        try {
            resp = new RestTemplate().getForObject(url, String.class);
        } catch (Exception e) {
            throw new BusinessException("请求微信接口失败：" + e.getMessage());
        }
        JSONObject wxResult = resp == null ? null : JSON.parseObject(resp);
        if (wxResult == null || wxResult.getString("openid") == null) {
            String errMsg = wxResult != null ? wxResult.getString("errmsg") : "未知错误";
            throw new BusinessException("微信授权失败：" + errMsg);
        }
        String openid = wxResult.getString("openid");
        // 用openid查询或创建用户
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getOpenid, openid);
        AppUser user = appUserMapper.selectOne(wrapper);
        if (user == null) {
            user = new AppUser();
            user.setOpenid(openid);
            user.setNickname("新同学");
            user.setStatus(1);
            user.setIdentityVerified(0);
            user.setSchoolVerified(0);
            appUserMapper.insert(user);
        }
        return Result.success(user);
    }

    /**
     * 发送短信验证码
     * 开发模式固定返回123456 生产模式调用阿里云短信发送随机验证码
     */
    @PostMapping("/sendCode")
    public Result<Map<String, Object>> sendCode(@RequestParam String phone) {
        SmsService.SendCodeResult result = smsService.sendCode(phone);
        Map<String, Object> map = new HashMap<>();
        map.put("expireMinutes", result.expireMinutes);
        map.put("expireSeconds", result.expireSeconds);
        map.put("message", "验证码已发送，" + result.expireMinutes + "分钟内有效");
        if (smsService.isMockSms()) {
            map.put("code", result.code);
        }
        return Result.success(map);
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
    public Result<Void> submitPersonal(@RequestBody PersonalAuth auth,
                                       @RequestParam(required = false) String smsCode) {
        if (auth.getUserId() == null) {
            throw new BusinessException("缺少用户ID");
        }
        if (auth.getPhone() == null || !auth.getPhone().matches("^1\\d{10}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        // 校验短信验证码
        if (!smsService.verifyCode(auth.getPhone(), smsCode)) {
            throw new BusinessException("验证码错误或已过期，请在" + smsService.getCodeExpireMinutes() + "分钟内使用");
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
