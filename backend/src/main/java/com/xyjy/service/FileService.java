package com.xyjy.service;

import com.xyjy.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件上传服务 本地存储 不使用外部URL
 */
@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    /**
     * 上传文件到根目录uploads文件夹下 返回可访问的相对路径
     */
    public String upload(MultipartFile file, String bizDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        // 按业务分目录 再按日期分目录
        String dateDir = LocalDate.now().toString();
        String relativeDir = bizDir + "/" + dateDir;
        File targetDir = new File(uploadDir, relativeDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        File targetFile = new File(targetDir, fileName);
        try {
            file.transferTo(targetFile.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
        // 返回可访问路径
        return accessPrefix + "/" + relativeDir + "/" + fileName;
    }

    /** 根据上传返回路径解析本地文件 */
    public File resolveUploadedFile(String urlPath) {
        if (urlPath == null || !urlPath.startsWith(accessPrefix + "/")) {
            return null;
        }
        String relative = urlPath.substring(accessPrefix.length() + 1);
        return new File(uploadDir, relative);
    }

    /** 删除已上传文件 */
    public void deleteUploadedFile(String urlPath) {
        File file = resolveUploadedFile(urlPath);
        if (file != null && file.exists() && !file.delete()) {
            throw new BusinessException("违规文件清理失败");
        }
    }
}
