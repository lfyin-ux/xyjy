package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.entity.SysAppSetting;
import com.xyjy.mapper.SysAppSettingMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 应用功能开关（数据库优先，未配置时回退 application.yml）
 */
@Service
public class AppFeatureService {

    public static final String KEY_MALL_ENABLED = "mall_enabled";

    @Value("${app.mall-enabled:true}")
    private boolean defaultMallEnabled;

    @Resource
    private SysAppSettingMapper sysAppSettingMapper;

    private volatile boolean mallEnabled;

    @PostConstruct
    public void init() {
        mallEnabled = readMallEnabledFromDb();
    }

    public boolean isMallEnabled() {
        return mallEnabled;
    }

    public void setMallEnabled(boolean enabled) {
        mallEnabled = enabled;
        SysAppSetting row = sysAppSettingMapper.selectOne(new LambdaQueryWrapper<SysAppSetting>()
                .eq(SysAppSetting::getConfigKey, KEY_MALL_ENABLED));
        if (row == null) {
            row = new SysAppSetting();
            row.setConfigKey(KEY_MALL_ENABLED);
            row.setConfigValue(Boolean.toString(enabled));
            row.setUpdateTime(LocalDateTime.now());
            sysAppSettingMapper.insert(row);
        } else {
            row.setConfigValue(Boolean.toString(enabled));
            row.setUpdateTime(LocalDateTime.now());
            sysAppSettingMapper.updateById(row);
        }
    }

    private boolean readMallEnabledFromDb() {
        try {
            SysAppSetting row = sysAppSettingMapper.selectOne(new LambdaQueryWrapper<SysAppSetting>()
                    .eq(SysAppSetting::getConfigKey, KEY_MALL_ENABLED));
            if (row != null && row.getConfigValue() != null) {
                return parseBool(row.getConfigValue(), defaultMallEnabled);
            }
        } catch (Exception ignored) {
            // 表未迁移时仍可用 yml 默认值
        }
        return defaultMallEnabled;
    }

    private static boolean parseBool(String raw, boolean fallback) {
        String v = raw.trim().toLowerCase();
        if ("true".equals(v) || "1".equals(v)) return true;
        if ("false".equals(v) || "0".equals(v)) return false;
        return fallback;
    }
}
