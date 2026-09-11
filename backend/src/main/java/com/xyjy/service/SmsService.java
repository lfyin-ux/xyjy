package com.xyjy.service;

import cn.hutool.core.util.RandomUtil;
import com.xyjy.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务
 * 未开启短信或开发模式：不发真实短信，验证码固定 123456
 * 开启短信后：调用阿里云短信 API 发送随机验证码
 */
@Service
public class SmsService {

    @Value("${app.mode:dev}")
    private String appMode;
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    @Value("${sms.access-key-id:}")
    private String accessKeyId;
    @Value("${sms.access-key-secret:}")
    private String accessKeySecret;
    @Value("${sms.sign-name:}")
    private String signName;
    @Value("${sms.template-code:}")
    private String templateCode;

    // 验证码缓存 key=手机号 value=验证码+过期时间
    private static final ConcurrentHashMap<String, CodeInfo> CODE_CACHE = new ConcurrentHashMap<>();

    // 发送频率限制 60秒一次
    private static final ConcurrentHashMap<String, Long> SEND_LIMIT = new ConcurrentHashMap<>();

    /** 是否使用模拟验证码（未对接短信或开发模式） */
    public boolean isMockSms() {
        return !smsEnabled || "dev".equals(appMode);
    }

    /**
     * 发送验证码
     */
    public String sendCode(String phone) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }

        boolean mock = isMockSms();
        long limitMs = mock ? 10000 : 60000;

        // 频率限制
        Long lastSend = SEND_LIMIT.get(phone);
        if (lastSend != null && System.currentTimeMillis() - lastSend < limitMs) {
            long waitSec = (limitMs - (System.currentTimeMillis() - lastSend) + 999) / 1000;
            throw new BusinessException("验证码发送过于频繁，请" + waitSec + "秒后重试");
        }

        String code;
        if (mock) {
            code = "123456";
        } else {
            code = RandomUtil.randomNumbers(6);
            boolean sent = sendSmsToPhone(phone, code);
            if (!sent) {
                throw new BusinessException("短信发送失败，请稍后重试");
            }
        }

        // 缓存验证码 5分钟有效
        CODE_CACHE.put(phone, new CodeInfo(code, System.currentTimeMillis() + 5 * 60 * 1000));
        SEND_LIMIT.put(phone, System.currentTimeMillis());
        return code;
    }

    /**
     * 校验验证码
     */
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) return false;

        // 模拟模式 123456 有效
        if (isMockSms() && "123456".equals(code)) {
            return true;
        }

        CodeInfo info = CODE_CACHE.get(phone);
        if (info == null) return false;
        if (System.currentTimeMillis() > info.expireTime) {
            CODE_CACHE.remove(phone);
            return false;
        }
        if (info.code.equals(code)) {
            CODE_CACHE.remove(phone);
            return true;
        }
        return false;
    }

    /**
     * 调用阿里云短信API发送验证码
     * 生产环境需要引入阿里云SDK 这里用HTTP方式简化示意
     * 实际对接时替换为官方SDK调用
     */
    private boolean sendSmsToPhone(String phone, String code) {
        try {
            // TODO: 替换为阿里云短信SDK调用
            // 示例伪代码：
            // DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            // IAcsClient client = new DefaultAcsClient(profile);
            // CommonRequest request = new CommonRequest();
            // request.setSysDomain("dysmsapi.aliyuncs.com");
            // request.setSysAction("SendSms");
            // request.putQueryParameter("PhoneNumbers", phone);
            // request.putQueryParameter("SignName", signName);
            // request.putQueryParameter("TemplateCode", templateCode);
            // request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");
            // CommonResponse response = client.getCommonResponse(request);
            // return response.getData().contains("OK");

            System.out.println("[SMS] 发送验证码到 " + phone + ": " + code);
            System.out.println("[SMS] 使用签名: " + signName + " 模板: " + templateCode);
            // 当前直接返回true 等配置好SDK后替换
            return true;
        } catch (Exception e) {
            System.err.println("[SMS] 发送失败: " + e.getMessage());
            return false;
        }
    }

    private static class CodeInfo {
        String code;
        long expireTime;

        CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}
