package com.xyjy.service;

import com.aliyun.cloudauth20190307.Client;
import com.aliyun.cloudauth20190307.models.ContrastFaceVerifyRequest;
import com.aliyun.cloudauth20190307.models.ContrastFaceVerifyResponse;
import com.aliyun.cloudauth20190307.models.ContrastFaceVerifyResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.xyjy.common.BusinessException;
import com.xyjy.common.IdCardUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云金融级实人认证 · 纯服务端照片核身（ID_MIN）
 */
@Service
public class AliyunFaceVerifyService {

    private static final List<String> ENDPOINTS = List.of(
            "cloudauth.cn-shanghai.aliyuncs.com",
            "cloudauth.cn-beijing.aliyuncs.com"
    );

    @Value("${aliyun.cloudauth.access-key-id:}")
    private String accessKeyId;
    @Value("${aliyun.cloudauth.access-key-secret:}")
    private String accessKeySecret;
    @Value("${aliyun.cloudauth.scene-id:0}")
    private Long sceneId;
    @Value("${sms.access-key-id:}")
    private String smsAccessKeyId;
    @Value("${sms.access-key-secret:}")
    private String smsAccessKeySecret;

    public boolean isConfigured() {
        return effectiveAccessKeyId() != null && !effectiveAccessKeyId().isEmpty()
                && effectiveAccessKeySecret() != null && !effectiveAccessKeySecret().isEmpty()
                && sceneId != null && sceneId > 0;
    }

    private String effectiveAccessKeyId() {
        if (accessKeyId != null && !accessKeyId.isEmpty()
                && !accessKeyId.startsWith("your_")) {
            return accessKeyId;
        }
        if (smsAccessKeyId != null && !smsAccessKeyId.isEmpty()
                && !smsAccessKeyId.startsWith("your_")) {
            return smsAccessKeyId;
        }
        return accessKeyId;
    }

    private String effectiveAccessKeySecret() {
        if (accessKeySecret != null && !accessKeySecret.isEmpty()
                && !accessKeySecret.startsWith("your_")) {
            return accessKeySecret;
        }
        if (smsAccessKeySecret != null && !smsAccessKeySecret.isEmpty()
                && !smsAccessKeySecret.startsWith("your_")) {
            return smsAccessKeySecret;
        }
        return accessKeySecret;
    }

    /**
     * 照片实人认证
     */
    public FaceVerifyResult verify(String realName, String idCard, String mobile, Long userId, byte[] faceImage) {
        if (faceImage == null || faceImage.length == 0) {
            throw new BusinessException("请上传人脸照片");
        }
        if (faceImage.length > 2 * 1024 * 1024) {
            throw new BusinessException("人脸照片过大，请重新拍摄");
        }
        String certNo = IdCardUtil.normalize(idCard);
        if (!IdCardUtil.isValid(certNo)) {
            throw new BusinessException("身份证号码校验未通过，请检查是否填写正确");
        }
        String outerOrderNo = UUID.randomUUID().toString().replace("-", "");
        ContrastFaceVerifyRequest request = new ContrastFaceVerifyRequest();
        request.setProductCode("ID_MIN");
        request.setCertType("IDENTITY_CARD");
        request.setSceneId(sceneId);
        request.setOuterOrderNo(outerOrderNo);
        request.setCertName(realName);
        request.setCertNo(certNo);
        request.setModel("FRONT_CAMERA_LIVENESS");
        request.setFaceContrastPicture(Base64.getEncoder().encodeToString(faceImage));
        if (mobile != null && !mobile.isEmpty()) {
            request.setMobile(mobile);
        }
        if (userId != null) {
            request.setUserId(String.valueOf(userId));
        }

        ContrastFaceVerifyResponse response = callWithFailover(request);
        ContrastFaceVerifyResponseBody body = response.getBody();
        FaceVerifyResult result = new FaceVerifyResult();
        result.outerOrderNo = outerOrderNo;
        if (body == null) {
            throw new BusinessException("人脸核身服务无响应");
        }
        result.requestId = body.getRequestId();
        result.code = body.getCode();
        result.message = body.getMessage();
        if (body.getResultObject() != null) {
            result.passed = "T".equalsIgnoreCase(body.getResultObject().getPassed());
            result.subCode = body.getResultObject().getSubCode();
        }
        if (!"200".equals(body.getCode())) {
            throw new BusinessException(mapApiError(body.getMessage()));
        }
        if (!result.passed) {
            throw new BusinessException(mapSubCodeMessage(result.subCode));
        }
        return result;
    }

    private String mapSubCodeMessage(String subCode) {
        if (subCode == null || subCode.isEmpty()) {
            return "人脸核身未通过，请重试";
        }
        switch (subCode) {
            case "201":
                return "姓名与身份证号不一致，请核对后重新填写";
            case "202":
                return "未查询到该身份信息，请确认身份证号是否正确";
            case "203":
                return "证件照片暂不可用，请稍后重试或联系客服人工审核";
            case "204":
                return "人脸与身份证照片不一致，请确认本人信息并在光线充足、正脸无遮挡的情况下重新拍照";
            case "205":
                return "活体检测未通过，请在明亮环境下正对镜头重新拍照";
            case "206":
                return "检测到多张人脸，请确保画面中只有本人正脸";
            case "207":
                return "人脸与身份证照片比对不一致，请核对本人信息后重试";
            case "209":
                return "权威数据源暂时异常，请稍后重试";
            case "210":
                return "未检测到人脸，请正对镜头重新拍照";
            case "211":
                return "人脸质量过低，请在光线充足处重新拍照";
            default:
                return "人脸核身未通过（" + subCode + "），请核对信息后重试";
        }
    }

    private String mapApiError(String message) {
        if (message == null || message.isEmpty()) {
            return "人脸核身服务异常";
        }
        if (message.contains("certNo")) {
            return "身份证号码无效，请填写本人真实18位身份证号";
        }
        if (message.contains("certName")) {
            return "姓名无效，请填写身份证上的真实姓名";
        }
        return message;
    }

    public FaceVerifyResult mockVerify(String realName, String idCard) {
        FaceVerifyResult result = new FaceVerifyResult();
        result.outerOrderNo = "mock" + UUID.randomUUID().toString().replace("-", "").substring(0, 26);
        result.passed = true;
        result.code = "200";
        result.message = "mock";
        return result;
    }

    private ContrastFaceVerifyResponse callWithFailover(ContrastFaceVerifyRequest request) {
        RuntimeOptions runtime = new RuntimeOptions();
        runtime.readTimeout = 15000;
        runtime.connectTimeout = 10000;
        Exception lastError = null;
        for (String endpoint : ENDPOINTS) {
            try {
                Client client = createClient(endpoint);
                ContrastFaceVerifyResponse response = client.contrastFaceVerifyWithOptions(request, runtime);
                if (response != null) {
                    if (response.getStatusCode() != null && response.getStatusCode() == 500) {
                        continue;
                    }
                    ContrastFaceVerifyResponseBody body = response.getBody();
                    if (body != null && "500".equals(body.getCode())) {
                        continue;
                    }
                }
                return response;
            } catch (Exception e) {
                lastError = e;
            }
        }
        String msg = lastError != null ? lastError.getMessage() : "未知错误";
        throw new BusinessException("人脸核身服务异常：" + msg);
    }

    private Client createClient(String endpoint) throws Exception {
        Config config = new Config();
        config.setAccessKeyId(effectiveAccessKeyId());
        config.setAccessKeySecret(effectiveAccessKeySecret());
        config.setEndpoint(endpoint);
        return new Client(config);
    }

    public static class FaceVerifyResult {
        public String outerOrderNo;
        public String requestId;
        public String code;
        public String message;
        public String subCode;
        public boolean passed;
    }
}
