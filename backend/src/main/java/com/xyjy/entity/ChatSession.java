package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话表
 */
@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userA;
    private Long userB;
    private String lastMsg;
    private LocalDateTime lastTime;
    private Integer locked;
    private Long initiator;
    private Integer status;
    private LocalDateTime createTime;
}
