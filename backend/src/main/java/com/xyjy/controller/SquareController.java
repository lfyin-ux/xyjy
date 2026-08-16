package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.*;
import com.xyjy.mapper.*;
import com.xyjy.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端 校园广场接口
 */
@RestController
@RequestMapping("/square")
public class SquareController {

    @Resource
    private SquarePostMapper squarePostMapper;
    @Resource
    private PostLikeMapper postLikeMapper;
    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private ReportRecordMapper reportRecordMapper;
    @Resource
    private FilterService filterService;
    @Resource
    private com.xyjy.service.BlacklistService blacklistService;

    /**
     * 广场动态列表 仅展示已发布 附带发布者信息
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String topic,
                                                   @RequestParam(required = false) Long userId) {
        Page<SquarePost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SquarePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SquarePost::getStatus, 3);
        if (topic != null && !topic.isEmpty()) {
            wrapper.eq(SquarePost::getTopic, topic);
        }
        wrapper.orderByDesc(SquarePost::getCreateTime);
        Page<SquarePost> result = squarePostMapper.selectPage(page, wrapper);

        // 黑名单双向过滤 隐藏与当前用户存在拉黑关系的动态
        List<SquarePost> records = result.getRecords();
        if (userId != null) {
            records = records.stream()
                    .filter(p -> !blacklistService.hasBlock(userId, p.getUserId()))
                    .collect(java.util.stream.Collectors.toList());
        }

        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(records.stream().map(this::toVo).collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    private Map<String, Object> toVo(SquarePost post) {
        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        AppUser user = appUserMapper.selectById(post.getUserId());
        map.put("user", user);
        return map;
    }

    /**
     * 动态详情
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        SquarePost post = squarePostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        return Result.success(toVo(post));
    }

    /**
     * 发布动态 分级审核
     */
    @PostMapping("/publish")
    public Result<Map<String, Object>> publish(@RequestBody SquarePost post) {
        if (post.getUserId() == null) {
            throw new BusinessException("缺少用户ID");
        }
        if (post.getContent() == null || post.getContent().isEmpty()) {
            throw new BusinessException("请输入动态内容");
        }
        FilterService.FilterResult fr = filterService.check(post.getContent(), "动态", post.getUserId());
        Map<String, Object> result = new HashMap<>();
        if (fr.level == 2) {
            // 违规拦截
            post.setStatus(5);
            result.put("status", "blocked");
            result.put("tip", fr.tip);
            return Result.error(fr.tip);
        } else if (fr.level == 1) {
            // 转人工审核
            post.setStatus(1);
            result.put("status", "pending");
            result.put("tip", "内容已提交，正在人工审核");
        } else {
            // 直接发布
            post.setStatus(3);
            result.put("status", "published");
            result.put("tip", "发布成功");
        }
        post.setLikeCount(0);
        post.setCommentCount(0);
        squarePostMapper.insert(post);
        return Result.success(result);
    }

    /**
     * 点赞或取消点赞
     */
    @PostMapping("/like")
    public Result<Map<String, Object>> like(@RequestParam Long postId, @RequestParam Long userId) {
        SquarePost post = squarePostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        PostLike exist = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId));
        Map<String, Object> result = new HashMap<>();
        if (exist != null) {
            postLikeMapper.deleteById(exist.getId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            result.put("liked", false);
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            result.put("liked", true);
        }
        squarePostMapper.updateById(post);
        result.put("likeCount", post.getLikeCount());
        return Result.success(result);
    }

    /**
     * 评论列表
     */
    @GetMapping("/comments/{postId}")
    public Result<List<Map<String, Object>>> comments(@PathVariable Long postId) {
        List<PostComment> list = postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId).eq(PostComment::getStatus, 3)
                .orderByAsc(PostComment::getCreateTime));
        List<Map<String, Object>> vos = list.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("comment", c);
            map.put("user", appUserMapper.selectById(c.getUserId()));
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 发布评论 分级审核
     */
    @PostMapping("/comment")
    public Result<String> comment(@RequestBody PostComment comment) {
        if (comment.getContent() == null || comment.getContent().isEmpty()) {
            throw new BusinessException("请输入评论内容");
        }
        FilterService.FilterResult fr = filterService.check(comment.getContent(), "评论", comment.getUserId());
        if (fr.level == 2) {
            return Result.error(fr.tip);
        }
        comment.setStatus(fr.level == 1 ? 1 : 3);
        postCommentMapper.insert(comment);
        // 更新评论数
        if (comment.getStatus() == 3) {
            SquarePost post = squarePostMapper.selectById(comment.getPostId());
            if (post != null) {
                post.setCommentCount(post.getCommentCount() + 1);
                squarePostMapper.updateById(post);
            }
        }
        return Result.success(fr.level == 1 ? "评论已提交审核" : "评论已发布");
    }

    /**
     * 删除自己的评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        PostComment comment = postCommentMapper.selectById(id);
        if (comment != null) {
            postCommentMapper.deleteById(id);
            SquarePost post = squarePostMapper.selectById(comment.getPostId());
            if (post != null && post.getCommentCount() > 0) {
                post.setCommentCount(post.getCommentCount() - 1);
                squarePostMapper.updateById(post);
            }
        }
        return Result.success();
    }

    /**
     * 删除自己的动态
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        SquarePost post = squarePostMapper.selectById(id);
        if (post != null) {
            post.setStatus(6);
            squarePostMapper.updateById(post);
        }
        return Result.success();
    }

    /**
     * 举报动态 评论或用户
     */
    @PostMapping("/report")
    public Result<Void> report(@RequestBody ReportRecord report) {
        if (report.getReporterId() == null) {
            throw new BusinessException("缺少举报人");
        }
        report.setStatus(0);
        reportRecordMapper.insert(report);
        return Result.success();
    }

    /**
     * 我的动态
     */
    @GetMapping("/my/{userId}")
    public Result<List<SquarePost>> myPosts(@PathVariable Long userId) {
        return Result.success(squarePostMapper.selectList(new LambdaQueryWrapper<SquarePost>()
                .eq(SquarePost::getUserId, userId).ne(SquarePost::getStatus, 6)
                .orderByDesc(SquarePost::getCreateTime)));
    }
}
