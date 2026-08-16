package com.xyjy.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket即时通讯端点
 * 连接地址 ws://host:port/api/ws/chat/{userId}
 * 用户上线时以userId为标识注册 下线时移除
 * 收到消息时解析目标用户ID直接推送
 */
@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocket {

    // 在线用户连接池 key为userId
    private static final ConcurrentHashMap<Long, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    // 当前连接的用户ID
    private Long userId;

    /**
     * 连接建立
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.userId = userId;
        ONLINE_SESSIONS.put(userId, session);
        System.out.println("[WS] 用户上线: " + userId + " 当前在线: " + ONLINE_SESSIONS.size());
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        if (userId != null) {
            ONLINE_SESSIONS.remove(userId);
            System.out.println("[WS] 用户下线: " + userId + " 当前在线: " + ONLINE_SESSIONS.size());
        }
    }

    /**
     * 收到客户端消息 解析并转发给目标用户
     * 消息格式 {"toId":2, "sessionId":1, "content":"hello", "msgType":1}
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject msg = JSON.parseObject(message);
            Long toId = msg.getLong("toId");
            if (toId == null) return;

            // 给消息加上发送者ID
            msg.put("fromId", userId);

            // 尝试推送给在线的目标用户
            Session targetSession = ONLINE_SESSIONS.get(toId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(msg.toJSONString());
            }
            // 无论对方在不在线 消息都会通过HTTP接口存库
            // WebSocket只负责实时推送 持久化由前端调原有的/chat/send接口完成
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("[WS] 连接异常 userId=" + userId + " " + error.getMessage());
        if (userId != null) {
            ONLINE_SESSIONS.remove(userId);
        }
    }

    /**
     * 向指定用户推送消息 供后端其他模块调用
     */
    public static void sendToUser(Long userId, String message) {
        Session session = ONLINE_SESSIONS.get(userId);
        System.out.println("[WS-PUSH] 推送给userId=" + userId + " 在线=" + (session != null && session.isOpen()) + " 当前在线人数=" + ONLINE_SESSIONS.size());
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                System.out.println("[WS-PUSH] 推送成功");
            } catch (IOException e) {
                System.out.println("[WS-PUSH] 推送失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断用户是否在线
     */
    public static boolean isOnline(Long userId) {
        Session session = ONLINE_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }
}
