package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题标签表
 */
@Data
@TableName("topic_tag")
public class TopicTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer postCount;
    private LocalDateTime createTime;
}
