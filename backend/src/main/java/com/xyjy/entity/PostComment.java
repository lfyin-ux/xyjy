package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态评论表
 */
@Data
@TableName("post_comment")
public class PostComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    /** 可见范围 1发布人可见 2回复人可见 3全部可见 */
    private Integer visibility;
    /** 回复对象用户ID visibility=2时必填 */
    private Long replyToUserId;
    /** 父评论ID */
    private Long parentId;
    private Integer status;
    private LocalDateTime createTime;
}
