package com.xyjy.controller;

import com.xyjy.common.Result;
import com.xyjy.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "bizDir", defaultValue = "common") String bizDir) {
        String url = fileService.upload(file, bizDir);
        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        return Result.success(map);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/uploadMulti")
    public Result<List<String>> uploadMulti(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(value = "bizDir", defaultValue = "common") String bizDir) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(fileService.upload(file, bizDir));
        }
        return Result.success(urls);
    }
}
