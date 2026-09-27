package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.Result;
import com.xyjy.config.AppProperties;
import com.xyjy.entity.SchoolInfo;
import com.xyjy.entity.TopicTag;
import com.xyjy.mapper.SchoolInfoMapper;
import com.xyjy.mapper.TopicTagMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共接口 学校字典与话题标签
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Resource
    private SchoolInfoMapper schoolInfoMapper;
    @Resource
    private TopicTagMapper topicTagMapper;
    @Resource
    private AppProperties appProperties;

    /**
     * 小程序端功能开关（无需登录）
     */
    @GetMapping("/app-config")
    public Result<Map<String, Object>> appConfig() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("mallEnabled", appProperties.isMallEnabled());
        return Result.success(cfg);
    }

    /**
     * 学校列表 支持模糊搜索
     */
    @GetMapping("/schools")
    public Result<List<SchoolInfo>> schools(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SchoolInfo> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SchoolInfo::getSchoolName, keyword);
        }
        wrapper.orderByAsc(SchoolInfo::getSchoolName);
        wrapper.last("limit 50");
        return Result.success(schoolInfoMapper.selectList(wrapper));
    }

    /**
     * 话题标签列表
     */
    @GetMapping("/topics")
    public Result<List<TopicTag>> topics() {
        return Result.success(topicTagMapper.selectList(new LambdaQueryWrapper<TopicTag>()
                .orderByDesc(TopicTag::getPostCount)));
    }
}
