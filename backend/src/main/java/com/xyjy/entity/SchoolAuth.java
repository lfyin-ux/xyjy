package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校认证表
 */
@Data
@TableName("school_auth")
public class SchoolAuth {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String schoolName;
    private String college;
    private String grade;
    private String studentNo;
    private String docType;
    private String docImgs;
    private String remark;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
