package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 过滤词命中记录表
 */
@Data
@TableName("filter_hit_log")
public class FilterHitLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String content;
    private Long userId;
    private String bizType;
    private String handleResult;
    private LocalDateTime createTime;
}
