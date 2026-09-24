package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 游戏组局表
 */
@Data
@TableName("game_team")
public class GameTeam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long creatorId;
    private Long schoolId;
    private String gameName;
    private String playTime;
    private Integer needNum;
    private Integer joinedNum;
    private String requireDesc;
    private String creatorContact;
    private Integer status;
    private LocalDateTime createTime;
}
