package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 匹配表
 */
@Data
@TableName("user_match")
public class UserMatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userA;
    private Long userB;
    private Integer status;
    private LocalDateTime createTime;
}
