package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学校字典表
 */
@Data
@TableName("school_info")
public class SchoolInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String schoolName;
    private String province;
    private String city;
}
