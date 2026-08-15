package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商城订单表
 */
@Data
@TableName("mall_order")
public class MallOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String address;
    private String receiver;
    private String phone;
    private LocalDateTime payTime;
    private Integer refundStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
