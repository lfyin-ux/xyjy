package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.AdminUserNames;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.PostComment;
import com.xyjy.entity.SquarePost;
import com.xyjy.entity.TopicTag;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.PostCommentMapper;
import com.xyjy.mapper.SquarePostMapper;
import com.xyjy.mapper.TopicTagMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 校园广场管理接口
 */
@RestController
@RequestMapping("/admin/square")
public class AdminSquareController {

    @Resource
    private SquarePostMapper squarePostMapper;
    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private TopicTagMapper topicTagMapper;
    @Resource
    private AppUserMapper appUserMapper;

    /**
     * 动态列表与搜索
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer status) {
        Page<SquarePost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SquarePost> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SquarePost::getContent, keyword);
        }
        if (status != null) {
            wrapper.eq(SquarePost::getStatus, status);
        }
        wrapper.orderByDesc(SquarePost::getCreateTime);
        Page<SquarePost> result = squarePostMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(p -> AdminUserNames.enrich(appUserMapper, p, p.getUserId(), "userName"))
                .collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 隐藏或删除动态
     */
    @PostMapping("/setStatus/{id}")
    public Result<Void> setStatus(@PathVariable Long id, @RequestParam Integer status) {
        SquarePost post = squarePostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        post.setStatus(status);
        squarePostMapper.updateById(post);
        return Result.success();
    }

    /**
     * 评论管理列表
     */
    @GetMapping("/comments")
    public Result<Page<PostComment>> comments(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PostComment> page = new Page<>(pageNum, pageSize);
        return Result.success(postCommentMapper.selectPage(page, new LambdaQueryWrapper<PostComment>()
                .orderByDesc(PostComment::getCreateTime)));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        postCommentMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 话题标签列表
     */
    @GetMapping("/topics")
    public Result<List<TopicTag>> topics() {
        return Result.success(topicTagMapper.selectList(new LambdaQueryWrapper<TopicTag>()
                .orderByDesc(TopicTag::getPostCount)));
    }

    /**
     * 新增或编辑话题标签
     */
    @PostMapping("/topic/save")
    public Result<Void> saveTopic(@RequestBody TopicTag tag) {
        if (tag.getName() == null || tag.getName().isEmpty()) {
            throw new BusinessException("请填写标签名称");
        }
        if (tag.getId() == null) {
            if (tag.getPostCount() == null) {
                tag.setPostCount(0);
            }
            topicTagMapper.insert(tag);
        } else {
            topicTagMapper.updateById(tag);
        }
        return Result.success();
    }

    /**
     * 删除话题标签
     */
    @DeleteMapping("/topic/{id}")
    public Result<Void> deleteTopic(@PathVariable Long id) {
        topicTagMapper.deleteById(id);
        return Result.success();
    }
}
