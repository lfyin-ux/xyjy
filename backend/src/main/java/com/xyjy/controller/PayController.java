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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付接口 支持开发模式（直接成功）和生产模式（微信支付）
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

    @Value("${app.mode:dev}")
    private String appMode;

    /**
     * 创建支付 前端下单后调用此接口获取支付参数
     * 开发模式直接标记已支付返回成功
     * 生产模式返回wx.requestPayment所需参数
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
            // 开发模式 直接标记已支付
            order.setStatus(2);
            order.setPayTime(LocalDateTime.now());
            mallOrderMapper.updateById(order);
            Map<String, String> result = new HashMap<>();
            result.put("mode", "dev");
            result.put("msg", "开发模式已自动完成支付");
            return Result.success(result);
        }

        // 生产模式 获取用户openid调用微信支付
        AppUser user = appUserMapper.selectById(order.getUserId());
        if (user == null || user.getOpenid() == null) {
            throw new BusinessException("用户信息异常");
        }
        // 金额转为分
        int totalFen = order.getTotalAmount().multiply(new BigDecimal("100")).intValue();
        String description = "碰个面商城订单";

        Map<String, String> payParams = wxPayService.createPrepay(
                order.getOrderNo(), totalFen, user.getOpenid(), description);
        return Result.success(payParams);
    }

    /**
     * 微信支付结果回调 微信服务器主动通知
     * 验签成功后更新订单状态
     */
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        try {
            // 读取微信推送的XML
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String xml = sb.toString();

            // 简单解析XML为Map
            Map<String, String> params = parseNotifyXml(xml);

            // 验签
            if (!wxPayService.verifyNotify(params)) {
                return "<xml><return_code>FAIL</return_code><return_msg>签名失败</return_msg></xml>";
            }

            String resultCode = params.get("result_code");
            String orderNo = params.get("out_trade_no");
            if ("SUCCESS".equals(resultCode) && orderNo != null) {
                // 更新订单为已支付
                MallOrder order = mallOrderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getOrderNo, orderNo));
                if (order != null && order.getStatus() == 1) {
                    order.setStatus(2);
                    order.setPayTime(LocalDateTime.now());
                    mallOrderMapper.updateById(order);
                }
            }
            return "<xml><return_code>SUCCESS</return_code><return_msg>OK</return_msg></xml>";
        } catch (Exception e) {
            return "<xml><return_code>FAIL</return_code><return_msg>" + e.getMessage() + "</return_msg></xml>";
        }
    }

    /**
     * 简单解析微信通知XML 生产环境建议用DOM解析
     */
    private Map<String, String> parseNotifyXml(String xml) {
        Map<String, String> map = new HashMap<>();
        String[] tags = {"return_code", "result_code", "out_trade_no", "transaction_id",
                "total_fee", "sign", "nonce_str", "openid"};
        for (String tag : tags) {
            String open = "<" + tag + ">";
            String close = "</" + tag + ">";
            int start = xml.indexOf(open);
            int end = xml.indexOf(close);
            if (start >= 0 && end >= 0) {
                String val = xml.substring(start + open.length(), end)
                        .replace("<![CDATA[", "").replace("]]>", "");
                map.put(tag, val);
            }
        }
        return map;
    }
}
