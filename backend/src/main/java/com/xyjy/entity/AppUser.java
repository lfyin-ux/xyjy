package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户主表
 */
@Data
@TableName("app_user")
public class AppUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private Integer age;
    private Integer height;
    private String intro;
    private String school;
    private String campus;
    private String college;
    private String grade;
    private String studentNo;
    private String tags;
    private String partnerType;
    private String expect;
    private String phone;
    private Integer identityVerified;
    private Integer schoolVerified;
    private Long schoolId;
    private Integer avatarAuditStatus;
    private Integer introAuditStatus;
    private Integer status;
    private LocalDateTime speakLimitEnd;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
