package com.xyjy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组局成员表
 */
@Data
@TableName("game_team_member")
public class GameTeamMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long userId;
    private LocalDateTime createTime;
}
