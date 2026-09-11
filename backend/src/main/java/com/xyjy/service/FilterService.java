package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.entity.FilterHitLog;
import com.xyjy.entity.FilterWord;
import com.xyjy.mapper.FilterHitLogMapper;
import com.xyjy.mapper.FilterWordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 内容过滤服务：微信内容安全 API + 本地词库分级审核
 */
@Service
public class FilterService {

    /** 面向用户的安全提示，不暴露具体命中词 */
    public static final String BLOCK_TIP = "所发布内容含违规信息";

    @Resource
    private FilterWordMapper filterWordMapper;
    @Resource
    private FilterHitLogMapper filterHitLogMapper;
    @Resource
    private WxSecCheckService wxSecCheckService;

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
     * 对内容进行分级检测（先微信内容安全，再本地词库）
     */
    public FilterResult check(String content, String bizType, Long userId) {
        if (content == null || content.isEmpty()) {
            return new FilterResult(0, null, null);
        }

        int scene = WxSecCheckService.sceneOf(bizType);
        WxSecCheckService.Suggest wxSuggest = wxSecCheckService.check(content, userId, scene);
        if (wxSuggest == WxSecCheckService.Suggest.RISKY) {
            saveHitLog("wx_sec_check", content, userId, bizType, "违规拦截");
            return new FilterResult(2, null, BLOCK_TIP);
        }
        if (wxSuggest == WxSecCheckService.Suggest.REVIEW) {
            saveHitLog("wx_sec_check", content, userId, bizType, "转人工审核");
            return new FilterResult(1, null, "内容已提交，正在人工审核");
        }

        LambdaQueryWrapper<FilterWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FilterWord::getEnabled, 1);
        List<FilterWord> words = filterWordMapper.selectList(wrapper);

        FilterWord auditHit = null;
        for (FilterWord w : words) {
            if (w.getScope() != null && !w.getScope().isEmpty()
                    && bizType != null && !w.getScope().contains(bizType)) {
                continue;
            }
            if (matched(content, w)) {
                if (w.getWordType() == 1) {
                    saveHitLog(w.getWord(), content, userId, bizType, "违规拦截");
                    return new FilterResult(2, w.getWord(), BLOCK_TIP);
                } else if (auditHit == null) {
                    auditHit = w;
                }
            }
        }
        if (auditHit != null) {
            saveHitLog(auditHit.getWord(), content, userId, bizType, "转人工审核");
            return new FilterResult(1, auditHit.getWord(), "内容已提交，正在人工审核");
        }
        return new FilterResult(0, null, null);
    }

    /**
     * 检测图片内容（微信 img_sec_check）
     */
    public FilterResult checkImage(File file, String bizType, Long userId) {
        if (file == null || !file.exists()) {
            return new FilterResult(0, null, null);
        }
        WxSecCheckService.Suggest wxSuggest = wxSecCheckService.checkImage(file);
        if (wxSuggest == WxSecCheckService.Suggest.RISKY) {
            saveHitLog("wx_img_check", bizType, userId, bizType, "违规拦截");
            return new FilterResult(2, null, BLOCK_TIP);
        }
        return new FilterResult(0, null, null);
    }

    /**
     * 违规时抛出统一提示
     */
    public void ensurePass(String content, String bizType, Long userId) {
        FilterResult fr = check(content, bizType, userId);
        if (fr.level == 2) {
            throw new com.xyjy.common.BusinessException(BLOCK_TIP);
        }
    }

    private boolean matched(String content, FilterWord w) {
        String word = w.getWord();
        if (word == null || word.isEmpty()) {
            return false;
        }
        Integer matchType = w.getMatchType() == null ? 1 : w.getMatchType();
        switch (matchType) {
            case 3:
                try {
                    return Pattern.compile(word).matcher(content).find();
                } catch (Exception e) {
                    return false;
                }
            case 2:
            case 1:
            default:
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
