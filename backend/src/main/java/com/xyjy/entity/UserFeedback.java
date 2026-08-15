package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 意见反馈表
 */
@Data
@TableName("user_feedback")
public class UserFeedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String contact;
    private Integer status;
    private LocalDateTime createTime;
}
