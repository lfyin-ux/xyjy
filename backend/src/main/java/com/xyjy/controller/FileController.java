package com.xyjy.controller;

import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.service.FileService;
import com.xyjy.service.FilterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传接口 本地存储
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;
    @Resource
    private FilterService filterService;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "bizDir", defaultValue = "common") String bizDir,
                                               @RequestParam(value = "userId", required = false) Long userId) {
        String url = fileService.upload(file, bizDir);
        ensureImageSafe(url, bizDir, userId);
        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        return Result.success(map);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/uploadMulti")
    public Result<List<String>> uploadMulti(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(value = "bizDir", defaultValue = "common") String bizDir,
                                            @RequestParam(value = "userId", required = false) Long userId) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = fileService.upload(file, bizDir);
            ensureImageSafe(url, bizDir, userId);
            urls.add(url);
        }
        return Result.success(urls);
    }

    private void ensureImageSafe(String url, String bizDir, Long userId) {
        if (!isImageBiz(bizDir)) {
            return;
        }
        File saved = fileService.resolveUploadedFile(url);
        FilterService.FilterResult fr = filterService.checkImage(saved, imageBizType(bizDir), userId);
        if (fr.level == 2) {
            fileService.deleteUploadedFile(url);
            throw new BusinessException(fr.tip);
        }
    }

    private boolean isImageBiz(String bizDir) {
        return bizDir != null && !bizDir.isEmpty();
    }

    private String imageBizType(String bizDir) {
        switch (bizDir) {
            case "avatar":
            case "photo":
                return "个人资料";
            case "post":
                return "动态";
            case "second":
                return "二手商品";
            case "chat":
                return "聊天";
            case "idcard":
            case "school":
                return "个人资料";
            default:
                return "图片";
        }
    }
}
