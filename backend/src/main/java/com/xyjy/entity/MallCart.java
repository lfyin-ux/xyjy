package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车表
 */
@Data
@TableName("mall_cart")
public class MallCart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goodsId;
    private String spec;
    private Integer quantity;
    private LocalDateTime createTime;
}
