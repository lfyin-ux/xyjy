package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.ChatMessage;
import com.xyjy.entity.ChatSession;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.ChatMessageMapper;
import com.xyjy.mapper.ChatSessionMapper;
import com.xyjy.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端 即时聊天接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatSessionMapper chatSessionMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private FilterService filterService;
    @Resource
    private com.xyjy.service.BlacklistService blacklistService;
    @Resource
    private com.xyjy.service.AuthCheckService authCheckService;

    /**
     * 会话列表
     */
    @GetMapping("/sessions/{userId}")
    public Result<List<Map<String, Object>>> sessions(@PathVariable Long userId) {
        // TODO: 小程序审核期间暂时关闭双认证门禁，审核通过后恢复
        authCheckService.requireFullAuth(userId);
        List<ChatSession> list = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getStatus, 1)
                .and(w -> w.eq(ChatSession::getUserA, userId).or().eq(ChatSession::getUserB, userId))
                .orderByDesc(ChatSession::getLastTime));
        List<Map<String, Object>> vos = list.stream()
                // 过滤掉与已拉黑或被拉黑用户的会话
                .filter(s -> {
                    Long otherId = s.getUserA().equals(userId) ? s.getUserB() : s.getUserA();
                    return !blacklistService.hasBlock(userId, otherId);
                })
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    Long otherId = s.getUserA().equals(userId) ? s.getUserB() : s.getUserA();
                    AppUser other = appUserMapper.selectById(otherId);
                    map.put("session", s);
                    map.put("other", other);
                    // 未读数
                    Long unread = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getSessionId, s.getId())
                            .eq(ChatMessage::getToId, userId)
                            .eq(ChatMessage::getIsRead, 0));
                    map.put("unread", unread);
                    return map;
                }).collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 会话消息记录 并将对方发来的消息标记已读
     */
    @GetMapping("/messages/{sessionId}")
    public Result<Map<String, Object>> messages(@PathVariable Long sessionId,
                                                 @RequestParam Long userId,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "20") Integer pageSize) {
        // 按时间倒序分页 最新消息在前
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        Page<ChatMessage> result = chatMessageMapper.selectPage(page,
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreateTime));
        // 反转为时间升序（旧消息在前）方便前端展示
        java.util.Collections.reverse(result.getRecords());
        // 标记本页中对方发给我的未读消息为已读
        result.getRecords().stream()
                .filter(m -> m.getToId().equals(userId) && m.getIsRead() != null && m.getIsRead() == 0)
                .forEach(m -> {
                    m.setIsRead(1);
                    chatMessageMapper.updateById(m);
                });
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("hasMore", pageNum * pageSize < result.getTotal());
        return Result.success(data);
    }

    /**
     * 发送消息 敏感词过滤与准入规则校验
     */
    @PostMapping("/send")
    public Result<ChatMessage> send(@RequestBody ChatMessage msg) {
        if (msg.getSessionId() == null || msg.getFromId() == null) {
            throw new BusinessException("参数缺失");
        }
        ChatSession session = chatSessionMapper.selectById(msg.getSessionId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 黑名单校验 存在拉黑关系时不能发送
        Long peerId = session.getUserA().equals(msg.getFromId()) ? session.getUserB() : session.getUserA();
        if (blacklistService.hasBlock(msg.getFromId(), peerId)) {
            throw new BusinessException("因黑名单关系，无法发送消息");
        }
        // 账号限制发言校验
        AppUser sender = appUserMapper.selectById(msg.getFromId());
        if (sender != null && sender.getStatus() != null && sender.getStatus() != 1) {
            throw new BusinessException("账号已被限制发言");
        }
        // 敏感词过滤
        FilterService.FilterResult fr = filterService.check(msg.getContent(), "聊天", msg.getFromId());
        if (fr.level == 2) {
            throw new BusinessException(fr.tip);
        }
        // 准入规则 会话锁定时 发起方在对方回复前不能继续发送
        if (session.getLocked() != null && session.getLocked() == 1) {
            if (session.getInitiator() != null && session.getInitiator().equals(msg.getFromId())) {
                throw new BusinessException("对方回复前不能继续发送消息");
            } else {
                // 接收方回复 解除锁定
                session.setLocked(0);
            }
        }
        // 确定接收方
        Long toId = session.getUserA().equals(msg.getFromId()) ? session.getUserB() : session.getUserA();
        msg.setToId(toId);
        if (msg.getMsgType() == null) {
            msg.setMsgType(1);
        }
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);
        // 更新会话
        session.setLastMsg(msg.getContent());
        session.setLastTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
        // 通过WebSocket实时推送给对方
        com.xyjy.websocket.ChatWebSocket.sendToUser(msg.getToId(),
                com.alibaba.fastjson2.JSON.toJSONString(msg));
        return Result.success(msg);
    }

    /**
     * 打招呼 首次创建会话 会话锁定待对方回复
     */
    @PostMapping("/hello")
    public Result<ChatSession> hello(@RequestParam Long fromId, @RequestParam Long toId,
                                     @RequestParam String content) {
        // 黑名单校验 存在拉黑关系时不能打招呼
        if (blacklistService.hasBlock(fromId, toId)) {
            throw new BusinessException("因黑名单关系，无法发送消息");
        }
        // 查询是否已有会话
        ChatSession session = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .and(w -> w.eq(ChatSession::getUserA, fromId).eq(ChatSession::getUserB, toId))
                .or(w -> w.eq(ChatSession::getUserA, toId).eq(ChatSession::getUserB, fromId)));
        if (session == null) {
            session = new ChatSession();
            session.setUserA(fromId);
            session.setUserB(toId);
            session.setLocked(1);
            session.setInitiator(fromId);
            session.setStatus(1);
            session.setLastMsg(content);
            session.setLastTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
            // 首条消息
            ChatMessage msg = new ChatMessage();
            msg.setSessionId(session.getId());
            msg.setFromId(fromId);
            msg.setToId(toId);
            msg.setMsgType(1);
            msg.setContent(content);
            msg.setIsRead(0);
            chatMessageMapper.insert(msg);
            // 通过WebSocket实时推送打招呼消息
            com.xyjy.websocket.ChatWebSocket.sendToUser(toId,
                    com.alibaba.fastjson2.JSON.toJSONString(msg));
        }
        return Result.success(session);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        ChatSession session = chatSessionMapper.selectById(id);
        if (session != null) {
            session.setStatus(0);
            chatSessionMapper.updateById(session);
        }
        return Result.success();
    }
}
