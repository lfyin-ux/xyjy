package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("personal_verify_daily")
public class PersonalVerifyDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate verifyDate;
    private LocalDateTime createTime;
}
