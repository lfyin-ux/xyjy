package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 过滤词库表
 */
@Data
@TableName("filter_word")
public class FilterWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private Integer wordType;
    private String category;
    private Integer riskLevel;
    private Integer matchType;
    private String scope;
    private String tip;
    private Integer enabled;
    private LocalDateTime createTime;
}
