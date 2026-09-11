package com.xyjy.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.xyjy.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务
 * 未开启短信或开发模式：不发真实短信，验证码固定 123456
 * 开启短信后：调用阿里云短信 API 发送随机验证码
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

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

    private static final ConcurrentHashMap<String, CodeInfo> CODE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> SEND_LIMIT = new ConcurrentHashMap<>();

    /** 是否使用模拟验证码（未对接短信或开发模式） */
    public boolean isMockSms() {
        return !smsEnabled || "dev".equals(appMode) || !isSmsConfigured();
    }

    private boolean isSmsConfigured() {
        return accessKeyId != null && !accessKeyId.isEmpty()
                && accessKeySecret != null && !accessKeySecret.isEmpty()
                && signName != null && !signName.isEmpty()
                && templateCode != null && !templateCode.isEmpty();
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
            sendSmsToPhone(phone, code);
        }

        CODE_CACHE.put(phone, new CodeInfo(code, System.currentTimeMillis() + 5 * 60 * 1000));
        SEND_LIMIT.put(phone, System.currentTimeMillis());
        return code;
    }

    /**
     * 校验验证码
     */
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) return false;

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

    private void sendSmsToPhone(String phone, String code) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            config.endpoint = "dysmsapi.aliyuncs.com";

            Client client = new Client(config);
            Map<String, String> params = new HashMap<>();
            params.put("code", code);

            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(JSON.toJSONString(params));

            SendSmsResponse response = client.sendSms(request);
            String respCode = response.getBody() == null ? null : response.getBody().getCode();
            if (!"OK".equalsIgnoreCase(respCode)) {
                String message = response.getBody() == null ? "未知错误" : response.getBody().getMessage();
                log.error("阿里云短信发送失败 phone={} code={} message={}", phone, respCode, message);
                throw new BusinessException("短信发送失败：" + message);
            }
            log.info("阿里云短信发送成功 phone={}", phone);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("阿里云短信发送异常 phone={}", phone, e);
            throw new BusinessException("短信发送失败，请稍后重试");
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
