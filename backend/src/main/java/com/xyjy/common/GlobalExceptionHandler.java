package com.xyjy.common;

import com.wechat.pay.java.core.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleWxPay(ServiceException e) {
        log.error("微信支付异常 code={} msg={}", e.getErrorCode(), e.getErrorMessage());
        return Result.error("支付服务暂时不可用，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
