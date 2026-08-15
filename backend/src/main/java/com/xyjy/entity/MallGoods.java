package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商城商品表
 */
@Data
@TableName("mall_goods")
public class MallGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    private String cover;
    private String images;
    private BigDecimal price;
    private String spec;
    private Integer stock;
    private Integer sales;
    private String detail;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
