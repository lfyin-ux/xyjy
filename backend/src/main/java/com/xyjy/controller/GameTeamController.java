package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.GameTeam;
import com.xyjy.entity.GameTeamMember;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.GameTeamMapper;
import com.xyjy.mapper.GameTeamMemberMapper;
import com.xyjy.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private FilterService filterService;

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
        String checkText = team.getGameName() + (team.getRequireDesc() == null ? "" : team.getRequireDesc());
        FilterService.FilterResult fr = filterService.check(checkText, "组局", team.getCreatorId());
        if (fr.level == 2) {
            throw new BusinessException(fr.tip);
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

    /**
     * 组局成员列表 查看某个组局有哪些人加入
     */
    @GetMapping("/members/{teamId}")
    public Result<List<Map<String, Object>>> members(@PathVariable Long teamId) {
        List<GameTeamMember> members = gameTeamMemberMapper.selectList(
                new LambdaQueryWrapper<GameTeamMember>().eq(GameTeamMember::getTeamId, teamId));
        List<Map<String, Object>> result = members.stream().map(m -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("member", m);
            map.put("user", appUserMapper.selectById(m.getUserId()));
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }

    /**
     * 我发起的组局
     */
    @GetMapping("/myCreated/{userId}")
    public Result<List<GameTeam>> myCreated(@PathVariable Long userId) {
        return Result.success(gameTeamMapper.selectList(new LambdaQueryWrapper<GameTeam>()
                .eq(GameTeam::getCreatorId, userId).orderByDesc(GameTeam::getCreateTime)));
    }

    /**
     * 我加入的组局 包含历史
     */
    @GetMapping("/myJoined/{userId}")
    public Result<List<GameTeam>> myJoined(@PathVariable Long userId) {
        List<GameTeamMember> joined = gameTeamMemberMapper.selectList(
                new LambdaQueryWrapper<GameTeamMember>().eq(GameTeamMember::getUserId, userId));
        List<Long> teamIds = joined.stream().map(GameTeamMember::getTeamId)
                .distinct().collect(java.util.stream.Collectors.toList());
        if (teamIds.isEmpty()) {
            return Result.success(new java.util.ArrayList<>());
        }
        return Result.success(gameTeamMapper.selectBatchIds(teamIds));
    }
}
