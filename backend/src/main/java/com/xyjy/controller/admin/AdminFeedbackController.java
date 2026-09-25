package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.AdminUserNames;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.UserFeedback;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.UserFeedbackMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台 意见反馈
 */
@RestController
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    @Resource
    private UserFeedbackMapper userFeedbackMapper;
    @Resource
    private AppUserMapper appUserMapper;

    /**
     * 意见反馈列表
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) Integer status) {
        Page<UserFeedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(UserFeedback::getStatus, status);
        }
        wrapper.orderByDesc(UserFeedback::getCreateTime);
        Page<UserFeedback> result = userFeedbackMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(f -> AdminUserNames.enrich(appUserMapper, f, f.getUserId(), "userName"))
                .collect(Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 标记已处理
     */
    @PostMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id) {
        UserFeedback feedback = userFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        feedback.setStatus(1);
        userFeedbackMapper.updateById(feedback);
        return Result.success();
    }
}
