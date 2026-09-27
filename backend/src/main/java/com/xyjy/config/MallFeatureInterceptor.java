package com.xyjy.config;

import com.xyjy.common.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 商城关闭时拦截用户端商城与收货地址接口
 */
@Component
public class MallFeatureInterceptor implements HandlerInterceptor {

    @Resource
    private AppProperties appProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!appProperties.isMallEnabled()) {
            throw new BusinessException("商城功能未开放");
        }
        return true;
    }
}
