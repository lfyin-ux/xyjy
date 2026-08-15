package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人认证表
 */
@Data
@TableName("personal_auth")
public class PersonalAuth {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String phone;
    private String realName;
    private String idCard;
    private String idFrontImg;
    private String idBackImg;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
