package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商城商品分类表
 */
@Data
@TableName("mall_category")
public class MallCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sort;
}
