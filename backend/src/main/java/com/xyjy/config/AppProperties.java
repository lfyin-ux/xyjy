package com.xyjy.config;

import com.xyjy.service.AppFeatureService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 应用级开关配置（委托 AppFeatureService，支持后台动态修改）
 */
@Component
public class AppProperties {

    @Resource
    private AppFeatureService appFeatureService;

    public boolean isMallEnabled() {
        return appFeatureService.isMallEnabled();
    }
}
