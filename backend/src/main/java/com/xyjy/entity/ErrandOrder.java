package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 校园跑腿订单表
 */
@Data
@TableName("errand_order")
public class ErrandOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long publisherId;
    private Long schoolId;
    private Long takerId;
    private String title;
    private String content;
    private String fromPlace;
    private String toPlace;
    private String finishTime;
    private BigDecimal fee;
    private String remark;
    private String publisherContact;
    private String takerContact;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
