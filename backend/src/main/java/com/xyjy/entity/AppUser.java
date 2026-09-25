package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
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
    private Long currentSchoolId;
    private BigDecimal mallTotalSpent;
    private Integer avatarAuditStatus;
    private Integer introAuditStatus;
    private Integer status;
    private LocalDateTime speakLimitEnd;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 列表展示：待审相册数量 */
    @TableField(exist = false)
    private Integer pendingPhotoCount;
    /** 列表展示：是否有待审资料/相册 */
    @TableField(exist = false)
    private Boolean pendingAudit;
    /** 列表展示：待审内容摘要，如「简介、相册×2」 */
    @TableField(exist = false)
    private String pendingAuditHint;
}
