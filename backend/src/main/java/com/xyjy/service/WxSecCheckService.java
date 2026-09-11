package com.xyjy.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xyjy.entity.AppUser;
import com.xyjy.mapper.AppUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信内容安全检测：文本 msg_sec_check、图片 img_sec_check
 */
@Service
public class WxSecCheckService {

    private static final Logger log = LoggerFactory.getLogger(WxSecCheckService.class);

    public enum Suggest {
        PASS, REVIEW, RISKY, SKIP
    }

    @Value("${wechat.app-id:}")
    private String appId;
    @Value("${wechat.app-secret:}")
    private String appSecret;
    @Value("${wechat.sec-check.enabled:true}")
    private boolean enabled;

    @Resource
    private AppUserMapper appUserMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken;
    private long tokenExpireAt;

    /**
     * 检测文本内容
     *
     * @param content 待检测文本
     * @param userId  用户ID，用于获取 openid
     * @param scene   场景值 1资料 2评论 3论坛 4社交日志
     */
    public Suggest check(String content, Long userId, int scene) {
        if (!enabled || !isConfigured() || content == null || content.isEmpty()) {
            return Suggest.SKIP;
        }
        String openid = resolveOpenid(userId);
        if (openid == null || openid.isEmpty()) {
            log.warn("微信内容安全跳过：用户{}无openid", userId);
            return Suggest.SKIP;
        }
        try {
            return doCheck(content, openid, scene);
        } catch (Exception e) {
            log.error("微信内容安全检测异常", e);
            return Suggest.SKIP;
        }
    }

    /**
     * 检测图片内容（同步，单张不超过 1MB）
     */
    public Suggest checkImage(File file) {
        if (!enabled || !isConfigured() || file == null || !file.exists()) {
            return Suggest.SKIP;
        }
        if (file.length() > 1024 * 1024) {
            log.warn("图片超过1MB，跳过微信图片安全检测: {}", file.getName());
            return Suggest.SKIP;
        }
        try {
            return doImageCheck(FileCopyUtils.copyToByteArray(file));
        } catch (IOException e) {
            log.error("读取图片失败", e);
            return Suggest.SKIP;
        }
    }

    private Suggest doImageCheck(byte[] imageData) {
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/img_sec_check?access_token=" + token;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<byte[]> entity = new HttpEntity<>(imageData, headers);

        String resp = restTemplate.postForObject(url, entity, String.class);
        if (resp == null) {
            return Suggest.SKIP;
        }
        JSONObject json = JSON.parseObject(resp);
        Integer errcode = json.getInteger("errcode");
        if (errcode != null && errcode == 87014) {
            return Suggest.RISKY;
        }
        if (errcode != null && errcode != 0) {
            log.warn("微信图片安全返回错误 errcode={} errmsg={}", errcode, json.getString("errmsg"));
            return Suggest.SKIP;
        }
        return Suggest.PASS;
    }

    private Suggest doCheck(String content, String openid, int scene) {
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("version", 2);
        body.put("openid", openid);
        body.put("scene", scene);
        body.put("content", content.length() > 2500 ? content.substring(0, 2500) : content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(body), headers);

        String resp = restTemplate.postForObject(url, entity, String.class);
        if (resp == null) {
            return Suggest.SKIP;
        }
        JSONObject json = JSON.parseObject(resp);
        Integer errcode = json.getInteger("errcode");
        if (errcode != null && errcode == 87014) {
            return Suggest.RISKY;
        }
        if (errcode != null && errcode != 0) {
            log.warn("微信内容安全返回错误 errcode={} errmsg={}", errcode, json.getString("errmsg"));
            return Suggest.SKIP;
        }
        JSONObject result = json.getJSONObject("result");
        if (result == null) {
            return Suggest.PASS;
        }
        String suggest = result.getString("suggest");
        if ("risky".equalsIgnoreCase(suggest)) {
            return Suggest.RISKY;
        }
        if ("review".equalsIgnoreCase(suggest)) {
            return Suggest.REVIEW;
        }
        return Suggest.PASS;
    }

    private String resolveOpenid(Long userId) {
        if (userId == null) {
            return null;
        }
        AppUser user = appUserMapper.selectById(userId);
        return user == null ? null : user.getOpenid();
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return accessToken;
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + appId + "&secret=" + appSecret;
        String resp = restTemplate.getForObject(url, String.class);
        JSONObject json = JSON.parseObject(resp);
        if (json == null || json.getString("access_token") == null) {
            throw new IllegalStateException("获取微信access_token失败: " + resp);
        }
        accessToken = json.getString("access_token");
        int expiresIn = json.getIntValue("expires_in", 7200);
        tokenExpireAt = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
        return accessToken;
    }

    private boolean isConfigured() {
        return appId != null && !appId.isEmpty() && !appId.contains("000000")
                && appSecret != null && !appSecret.isEmpty() && !appSecret.contains("your_app");
    }

    /**
     * 业务类型映射微信检测场景
     */
    public static int sceneOf(String bizType) {
        if (bizType == null) {
            return 2;
        }
        switch (bizType) {
            case "评论":
                return 2;
            case "动态":
            case "二手商品":
            case "跑腿":
            case "组局":
            case "意见反馈":
                return 3;
            case "聊天":
            case "个人资料":
                return 4;
            default:
                return 2;
        }
    }
}
