package com.xyjy.service;

import com.xyjy.common.BusinessException;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务（APIv3）
 * 开发模式返回模拟参数；生产模式调用 JSAPI 下单与退款接口
 */
@Service
public class WxPayService {

    private static final Logger log = LoggerFactory.getLogger(WxPayService.class);

    @Value("${app.mode:dev}")
    private String appMode;
    @Value("${wechat.app-id:}")
    private String appId;
    @Value("${wxpay.mch-id:}")
    private String mchId;
    @Value("${wxpay.api-v3-key:}")
    private String apiV3Key;
    @Value("${wxpay.merchant-serial-number:}")
    private String merchantSerialNumber;
    @Value("${wxpay.private-key-path:}")
    private String privateKeyPath;
    @Value("${wxpay.notify-url:}")
    private String notifyUrl;

    private volatile RSAAutoCertificateConfig payConfig;

    public boolean isConfigured() {
        return mchId != null && !mchId.isEmpty() && !mchId.contains("your_")
                && apiV3Key != null && !apiV3Key.isEmpty() && !apiV3Key.contains("your_")
                && merchantSerialNumber != null && !merchantSerialNumber.isEmpty()
                && privateKeyPath != null && !privateKeyPath.isEmpty();
    }

    /**
     * 创建预付单，返回前端 wx.requestPayment 所需参数
     */
    public Map<String, String> createPrepay(String orderNo, int totalFen, String openid, String description) {
        if ("dev".equals(appMode)) {
            Map<String, String> result = new HashMap<>();
            result.put("mode", "dev");
            result.put("orderNo", orderNo);
            return result;
        }
        if (!isConfigured()) {
            throw new BusinessException("微信支付未配置，请联系管理员");
        }

        PrepayRequest request = new PrepayRequest();
        request.setAppid(appId);
        request.setMchid(mchId);
        request.setDescription(description);
        request.setOutTradeNo(orderNo);
        request.setNotifyUrl(notifyUrl);

        Amount amount = new Amount();
        amount.setTotal(totalFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        JsapiServiceExtension service = new JsapiServiceExtension.Builder()
                .config(getConfig())
                .build();
        PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);

        Map<String, String> result = new HashMap<>();
        result.put("mode", "prod");
        result.put("timeStamp", response.getTimeStamp());
        result.put("nonceStr", response.getNonceStr());
        result.put("package", response.getPackageVal());
        result.put("signType", response.getSignType());
        result.put("paySign", response.getPaySign());
        return result;
    }

    /**
     * 申请退款
     */
    public boolean refund(String orderNo, String refundNo, int totalFen, int refundFen) {
        if ("dev".equals(appMode)) {
            return true;
        }
        if (!isConfigured()) {
            log.error("微信支付未配置，退款失败 orderNo={}", orderNo);
            return false;
        }

        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(orderNo);
        request.setOutRefundNo(refundNo);

        AmountReq amount = new AmountReq();
        amount.setRefund((long) refundFen);
        amount.setTotal((long) totalFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);

        RefundService service = new RefundService.Builder()
                .config(getConfig())
                .build();
        Refund refund = service.create(request);
        return refund != null && (refund.getStatus() == Status.SUCCESS
                || refund.getStatus() == Status.PROCESSING);
    }

    /**
     * 解析并验签支付回调，返回商户订单号
     */
    public String parsePayNotify(String serial, String nonce, String signature, String timestamp, String body) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body)
                .build();

        NotificationParser parser = new NotificationParser((NotificationConfig) getConfig());
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        if (transaction == null || transaction.getOutTradeNo() == null) {
            throw new BusinessException("支付回调解析失败");
        }
        if (transaction.getTradeState() != Transaction.TradeStateEnum.SUCCESS) {
            throw new BusinessException("支付未成功");
        }
        return transaction.getOutTradeNo();
    }

    private RSAAutoCertificateConfig getConfig() {
        if (payConfig == null) {
            synchronized (this) {
                if (payConfig == null) {
                    payConfig = new RSAAutoCertificateConfig.Builder()
                            .merchantId(mchId)
                            .privateKeyFromPath(privateKeyPath)
                            .merchantSerialNumber(merchantSerialNumber)
                            .apiV3Key(apiV3Key)
                            .build();
                }
            }
        }
        return payConfig;
    }
}
