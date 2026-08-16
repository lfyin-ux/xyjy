package com.xyjy.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.xyjy.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 微信支付服务
 * 开发模式直接返回模拟支付参数 生产模式调用微信统一下单API
 */
@Service
public class WxPayService {

    @Value("${app.mode:dev}")
    private String appMode;
    @Value("${wechat.app-id:}")
    private String appId;
    @Value("${wxpay.mch-id:}")
    private String mchId;
    @Value("${wxpay.api-key:}")
    private String apiKey;
    @Value("${wxpay.notify-url:}")
    private String notifyUrl;

    /**
     * 创建预付单 返回前端调起支付所需参数
     * 开发模式返回模拟参数 前端不调wx.requestPayment直接标记成功
     * 生产模式调用微信统一下单接口
     */
    public Map<String, String> createPrepay(String orderNo, int totalFen, String openid, String description) {
        if ("dev".equals(appMode)) {
            // 开发模式 返回模拟参数 前端据此判断跳过真实支付
            Map<String, String> result = new HashMap<>();
            result.put("mode", "dev");
            result.put("orderNo", orderNo);
            return result;
        }

        // 生产模式 调用微信统一下单API
        return unifiedOrder(orderNo, totalFen, openid, description);
    }

    /**
     * 申请退款 调用微信退款接口
     * 开发模式直接返回成功 生产模式调用微信退款API
     */
    public boolean refund(String orderNo, String refundNo, int totalFen, int refundFen) {
        if ("dev".equals(appMode)) {
            return true;
        }
        return doRefund(orderNo, refundNo, totalFen, refundFen);
    }

    /**
     * 调用微信退款接口
     * 注意：微信退款需要双向证书 生产环境需配置apiclient_cert.p12证书
     */
    private boolean doRefund(String orderNo, String refundNo, int totalFen, int refundFen) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", IdUtil.simpleUUID());
        params.put("out_trade_no", orderNo);
        params.put("out_refund_no", refundNo);
        params.put("total_fee", String.valueOf(totalFen));
        params.put("refund_fee", String.valueOf(refundFen));
        String signVal = sign(params);
        params.put("sign", signVal);

        String xml = mapToXml(params);
        RestTemplate rest = new RestTemplate();
        String respXml;
        try {
            respXml = rest.postForObject("https://api.mch.weixin.qq.com/secapi/pay/refund", xml, String.class);
        } catch (Exception e) {
            System.err.println("[退款] 调用微信退款接口失败: " + e.getMessage());
            return false;
        }
        String returnCode = extractXmlValue(respXml, "return_code");
        String resultCode = extractXmlValue(respXml, "result_code");
        if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
            return true;
        }
        String errMsg = extractXmlValue(respXml, "err_code_des");
        System.err.println("[退款] 微信退款失败: " + errMsg);
        return false;
    }

    /**
     * 微信统一下单 返回前端wx.requestPayment所需参数
     */
    private Map<String, String> unifiedOrder(String orderNo, int totalFen, String openid, String description) {
        // 构建请求参数
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", IdUtil.simpleUUID());
        params.put("body", description);
        params.put("out_trade_no", orderNo);
        params.put("total_fee", String.valueOf(totalFen));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);

        // 签名
        String sign = sign(params);
        params.put("sign", sign);

        // 转换为XML请求微信
        String xml = mapToXml(params);
        RestTemplate rest = new RestTemplate();
        String respXml;
        try {
            respXml = rest.postForObject("https://api.mch.weixin.qq.com/pay/unifiedorder", xml, String.class);
        } catch (Exception e) {
            throw new BusinessException("调用微信支付接口失败：" + e.getMessage());
        }

        // 解析响应获取prepay_id
        String prepayId = extractXmlValue(respXml, "prepay_id");
        String returnCode = extractXmlValue(respXml, "return_code");
        if (!"SUCCESS".equals(returnCode) || prepayId == null) {
            String errMsg = extractXmlValue(respXml, "return_msg");
            throw new BusinessException("微信支付下单失败：" + errMsg);
        }

        // 组装前端wx.requestPayment参数并二次签名
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = IdUtil.simpleUUID();
        String packageStr = "prepay_id=" + prepayId;

        TreeMap<String, String> payParams = new TreeMap<>();
        payParams.put("appId", appId);
        payParams.put("timeStamp", timeStamp);
        payParams.put("nonceStr", nonceStr);
        payParams.put("package", packageStr);
        payParams.put("signType", "MD5");
        String paySign = sign(payParams);

        Map<String, String> result = new HashMap<>();
        result.put("mode", "prod");
        result.put("timeStamp", timeStamp);
        result.put("nonceStr", nonceStr);
        result.put("package", packageStr);
        result.put("signType", "MD5");
        result.put("paySign", paySign);
        return result;
    }

    /**
     * 验证微信支付回调签名
     */
    public boolean verifyNotify(Map<String, String> params) {
        String sign = params.get("sign");
        if (sign == null) return false;
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("sign");
        return sign.equals(sign(sortedParams));
    }

    /**
     * MD5签名
     */
    private String sign(TreeMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key=").append(apiKey);
        return SecureUtil.md5(sb.toString()).toUpperCase();
    }

    private String mapToXml(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">")
              .append(entry.getValue())
              .append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    private String extractXmlValue(String xml, String tag) {
        if (xml == null) return null;
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);
        if (start < 0 || end < 0) return null;
        return xml.substring(start + open.length(), end).replace("<![CDATA[", "").replace("]]>", "");
    }
}
