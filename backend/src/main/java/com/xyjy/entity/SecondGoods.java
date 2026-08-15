package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手商品表
 */
@Data
@TableName("second_goods")
public class SecondGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sellerId;
    private String name;
    private String category;
    private String conditionDesc;
    private BigDecimal price;
    private String description;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
