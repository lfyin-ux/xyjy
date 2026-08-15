package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商城订单明细表
 */
@Data
@TableName("mall_order_item")
public class MallOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long goodsId;
    private String goodsName;
    private String goodsCover;
    private String spec;
    private BigDecimal price;
    private Integer quantity;
}
