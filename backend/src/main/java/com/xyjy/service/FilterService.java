package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.entity.FilterHitLog;
import com.xyjy.entity.FilterWord;
import com.xyjy.mapper.FilterHitLogMapper;
import com.xyjy.mapper.FilterWordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 内容过滤服务 实现分级审核逻辑
 * 先检测违规词直接拦截 再检测审核词转人工 都未命中直接发布
 */
@Service
public class FilterService {

    @Resource
    private FilterWordMapper filterWordMapper;
    @Resource
    private FilterHitLogMapper filterHitLogMapper;

    /**
     * 检测结果 0通过 1需人工审核 2违规拦截
     */
    public static class FilterResult {
        public int level;
        public String hitWord;
        public String tip;

        public FilterResult(int level, String hitWord, String tip) {
            this.level = level;
            this.hitWord = hitWord;
            this.tip = tip;
        }
    }

    /**
     * 对内容进行分级检测
     * bizType 业务类型 用于范围过滤和记录
     */
    public FilterResult check(String content, String bizType, Long userId) {
        if (content == null || content.isEmpty()) {
            return new FilterResult(0, null, null);
        }
        // 查询启用的词库 违规词优先级更高 先排违规词
        LambdaQueryWrapper<FilterWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FilterWord::getEnabled, 1);
        List<FilterWord> words = filterWordMapper.selectList(wrapper);

        FilterWord auditHit = null;
        for (FilterWord w : words) {
            // 适用范围过滤
            if (w.getScope() != null && !w.getScope().isEmpty()
                    && bizType != null && !w.getScope().contains(bizType)) {
                continue;
            }
            if (matched(content, w)) {
                if (w.getWordType() == 1) {
                    // 命中违规词 直接拦截
                    saveHitLog(w.getWord(), content, userId, bizType, "违规拦截");
                    String tip = (w.getTip() != null && !w.getTip().isEmpty()) ? w.getTip() : "内容包含违规信息，禁止发布";
                    return new FilterResult(2, w.getWord(), tip);
                } else if (auditHit == null) {
                    // 记录审核词命中 继续检测是否有违规词
                    auditHit = w;
                }
            }
        }
        if (auditHit != null) {
            saveHitLog(auditHit.getWord(), content, userId, bizType, "转人工审核");
            return new FilterResult(1, auditHit.getWord(), "内容需要人工审核");
        }
        return new FilterResult(0, null, null);
    }

    /**
     * 根据匹配方式判断是否命中
     */
    private boolean matched(String content, FilterWord w) {
        String word = w.getWord();
        if (word == null || word.isEmpty()) {
            return false;
        }
        Integer matchType = w.getMatchType() == null ? 1 : w.getMatchType();
        switch (matchType) {
            case 3:
                // 正则匹配
                try {
                    return Pattern.compile(word).matcher(content).find();
                } catch (Exception e) {
                    return false;
                }
            case 2:
            case 1:
            default:
                // 精确和模糊统一按包含处理 忽略大小写
                return content.toLowerCase().contains(word.toLowerCase());
        }
    }

    private void saveHitLog(String word, String content, Long userId, String bizType, String result) {
        FilterHitLog log = new FilterHitLog();
        log.setWord(word);
        log.setContent(content.length() > 500 ? content.substring(0, 500) : content);
        log.setUserId(userId);
        log.setBizType(bizType);
        log.setHandleResult(result);
        filterHitLogMapper.insert(log);
    }
}
