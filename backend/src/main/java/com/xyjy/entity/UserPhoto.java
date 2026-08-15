package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户相册表
 */
@Data
@TableName("user_photo")
public class UserPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String imgUrl;
    private Integer sort;
    private Integer auditStatus;
    private LocalDateTime createTime;
}
