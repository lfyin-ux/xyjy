package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违规记录表
 */
@Data
@TableName("violation_record")
public class ViolationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private String reason;
    private String adminName;
    private LocalDateTime createTime;
}
