package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.*;
import com.xyjy.mapper.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 内容审核接口 举报 违规处理
 */
@RestController
@RequestMapping("/admin/content")
public class AdminContentController {

    @Resource
    private SquarePostMapper squarePostMapper;
    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private ReportRecordMapper reportRecordMapper;
    @Resource
    private FilterHitLogMapper filterHitLogMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private AppUserMapper appUserMapper;

    /**
     * 待审核动态列表 待审核状态
     */
    @GetMapping("/post/auditList")
    public Result<Page<Map<String, Object>>> postAuditList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SquarePost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SquarePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SquarePost::getStatus, 1).orderByDesc(SquarePost::getCreateTime);
        Page<SquarePost> result = squarePostMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("post", p);
            map.put("user", appUserMapper.selectById(p.getUserId()));
            return map;
        }).collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 动态审核通过
     */
    @PostMapping("/post/pass/{id}")
    public Result<Void> postPass(@PathVariable Long id) {
        SquarePost post = squarePostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        post.setStatus(3);
        squarePostMapper.updateById(post);
        return Result.success();
    }

    /**
     * 动态审核驳回
     */
    @PostMapping("/post/reject/{id}")
    public Result<Void> postReject(@PathVariable Long id, @RequestParam String reason) {
        SquarePost post = squarePostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        post.setStatus(4);
        post.setRejectReason(reason);
        squarePostMapper.updateById(post);
        return Result.success();
    }

    /**
     * 待审核评论列表
     */
    @GetMapping("/comment/auditList")
    public Result<List<PostComment>> commentAuditList() {
        return Result.success(postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getStatus, 1).orderByDesc(PostComment::getCreateTime)));
    }

    /**
     * 评论审核 pass通过则发布 否则拦截
     */
    @PostMapping("/comment/audit")
    public Result<Void> commentAudit(@RequestParam Long id, @RequestParam Boolean pass) {
        PostComment comment = postCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        comment.setStatus(pass ? 3 : 5);
        postCommentMapper.updateById(comment);
        if (pass && comment.getVisibility() != null && comment.getVisibility() == 3) {
            SquarePost post = squarePostMapper.selectById(comment.getPostId());
            if (post != null) {
                post.setCommentCount(post.getCommentCount() + 1);
                squarePostMapper.updateById(post);
            }
        }
        return Result.success();
    }

    /**
     * 举报列表
     */
    @GetMapping("/report/list")
    public Result<Page<ReportRecord>> reportList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) Integer status) {
        Page<ReportRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ReportRecord::getStatus, status);
        }
        wrapper.orderByDesc(ReportRecord::getCreateTime);
        return Result.success(reportRecordMapper.selectPage(page, wrapper));
    }

    /**
     * 处理举报
     */
    @PostMapping("/report/handle/{id}")
    public Result<Void> handleReport(@PathVariable Long id, @RequestParam Integer status,
                                     @RequestParam(required = false) String result) {
        ReportRecord report = reportRecordMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("举报不存在");
        }
        report.setStatus(status);
        report.setHandleResult(result);
        reportRecordMapper.updateById(report);
        return Result.success();
    }

    /**
     * 过滤词命中记录查询
     */
    @GetMapping("/hitLog")
    public Result<Page<FilterHitLog>> hitLog(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String bizType) {
        Page<FilterHitLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FilterHitLog> wrapper = new LambdaQueryWrapper<>();
        if (bizType != null && !bizType.isEmpty()) {
            wrapper.eq(FilterHitLog::getBizType, bizType);
        }
        wrapper.orderByDesc(FilterHitLog::getCreateTime);
        return Result.success(filterHitLogMapper.selectPage(page, wrapper));
    }

    /**
     * 用户聊天记录查询
     */
    @GetMapping("/chatLog")
    public Result<List<ChatMessage>> chatLog(@RequestParam Long sessionId) {
        return Result.success(chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId).orderByAsc(ChatMessage::getCreateTime)));
    }
}
