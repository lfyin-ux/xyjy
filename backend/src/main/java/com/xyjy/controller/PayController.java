package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.MallOrder;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.MallOrderMapper;
import com.xyjy.service.WxPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付接口 支持开发模式（直接成功）和生产模式（微信支付 APIv3）
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private WxPayService wxPayService;
    @Resource
    private com.xyjy.service.MallSpendService mallSpendService;

    @Value("${app.mode:dev}")
    private String appMode;

    /**
     * 创建支付 前端下单后调用此接口获取支付参数
     */
    @PostMapping("/create/{orderId}")
    public Result<Map<String, String>> createPay(@PathVariable Long orderId) {
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态异常，不可支付");
        }

        if ("dev".equals(appMode)) {
            order.setStatus(2);
            order.setPayTime(LocalDateTime.now());
            mallOrderMapper.updateById(order);
            mallSpendService.refreshUserSpend(order.getUserId());
            Map<String, String> result = new HashMap<>();
            result.put("mode", "dev");
            result.put("msg", "开发模式已自动完成支付");
            return Result.success(result);
        }

        AppUser user = appUserMapper.selectById(order.getUserId());
        if (user == null || user.getOpenid() == null) {
            throw new BusinessException("用户信息异常");
        }
        int totalFen = order.getTotalAmount().multiply(new BigDecimal("100")).intValue();
        String description = "同行时空商城订单";

        Map<String, String> payParams = wxPayService.createPrepay(
                order.getOrderNo(), totalFen, user.getOpenid(), description);
        return Result.success(payParams);
    }

    /**
     * 微信支付 APIv3 结果回调
     */
    @PostMapping("/notify")
    public ResponseEntity<Void> payNotify(HttpServletRequest request) {
        try {
            String body = readBody(request);
            String orderNo = wxPayService.parsePayNotify(
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"),
                    body);

            MallOrder order = mallOrderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                    .eq(MallOrder::getOrderNo, orderNo));
            if (order != null && order.getStatus() == 1) {
                order.setStatus(2);
                order.setPayTime(LocalDateTime.now());
                mallOrderMapper.updateById(order);
                mallSpendService.refreshUserSpend(order.getUserId());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String readBody(HttpServletRequest request) throws Exception {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
