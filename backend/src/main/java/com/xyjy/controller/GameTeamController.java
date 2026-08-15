package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.GameTeam;
import com.xyjy.entity.GameTeamMember;
import com.xyjy.mapper.GameTeamMapper;
import com.xyjy.mapper.GameTeamMemberMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 游戏组局接口
 */
@RestController
@RequestMapping("/game")
public class GameTeamController {

    @Resource
    private GameTeamMapper gameTeamMapper;
    @Resource
    private GameTeamMemberMapper gameTeamMemberMapper;

    /**
     * 组局列表
     */
    @GetMapping("/list")
    public Result<List<GameTeam>> list() {
        return Result.success(gameTeamMapper.selectList(new LambdaQueryWrapper<GameTeam>()
                .orderByDesc(GameTeam::getCreateTime)));
    }

    /**
     * 组局详情
     */
    @GetMapping("/detail/{id}")
    public Result<GameTeam> detail(@PathVariable Long id) {
        GameTeam team = gameTeamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException("组局不存在");
        }
        return Result.success(team);
    }

    /**
     * 发起新组局
     */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody GameTeam team) {
        if (team.getCreatorId() == null) {
            throw new BusinessException("缺少发起人");
        }
        if (team.getGameName() == null || team.getGameName().isEmpty()) {
            throw new BusinessException("请填写游戏名称");
        }
        team.setJoinedNum(1);
        team.setStatus(1);
        gameTeamMapper.insert(team);
        // 发起人自动加入
        GameTeamMember member = new GameTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(team.getCreatorId());
        gameTeamMemberMapper.insert(member);
        return Result.success();
    }

    /**
     * 申请加入组局
     */
    @PostMapping("/join")
    public Result<Void> join(@RequestParam Long teamId, @RequestParam Long userId) {
        GameTeam team = gameTeamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("组局不存在");
        }
        if (team.getStatus() != 1) {
            throw new BusinessException("组局已满或已结束");
        }
        Long exist = gameTeamMemberMapper.selectCount(new LambdaQueryWrapper<GameTeamMember>()
                .eq(GameTeamMember::getTeamId, teamId).eq(GameTeamMember::getUserId, userId));
        if (exist != null && exist > 0) {
            throw new BusinessException("你已加入该组局");
        }
        GameTeamMember member = new GameTeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        gameTeamMemberMapper.insert(member);
        team.setJoinedNum(team.getJoinedNum() + 1);
        if (team.getJoinedNum() >= team.getNeedNum()) {
            team.setStatus(2);
        }
        gameTeamMapper.updateById(team);
        return Result.success();
    }

    /**
     * 退出组局
     */
    @PostMapping("/quit")
    public Result<Void> quit(@RequestParam Long teamId, @RequestParam Long userId) {
        GameTeam team = gameTeamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("组局不存在");
        }
        int deleted = gameTeamMemberMapper.delete(new LambdaQueryWrapper<GameTeamMember>()
                .eq(GameTeamMember::getTeamId, teamId).eq(GameTeamMember::getUserId, userId));
        if (deleted > 0) {
            team.setJoinedNum(Math.max(1, team.getJoinedNum() - 1));
            if (team.getStatus() == 2 && team.getJoinedNum() < team.getNeedNum()) {
                team.setStatus(1);
            }
            gameTeamMapper.updateById(team);
        }
        return Result.success();
    }
}
