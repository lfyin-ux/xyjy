package com.xyjy.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸核身成功会话（提交认证时一次性校验）
 */
@Service
public class FaceVerifySessionService {

    private static final long TTL_MS = 30 * 60 * 1000L;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void save(Long userId, String verifyNo, String realName, String idCard) {
        String key = key(userId, verifyNo);
        Session session = new Session();
        session.userId = userId;
        session.verifyNo = verifyNo;
        session.realName = realName;
        session.idCard = idCard.trim().toUpperCase();
        session.passed = true;
        session.expireAt = System.currentTimeMillis() + TTL_MS;
        sessions.put(key, session);
    }

    public boolean consume(Long userId, String verifyNo, String realName, String idCard) {
        String key = key(userId, verifyNo);
        Session session = sessions.get(key);
        if (session == null || session.expireAt < System.currentTimeMillis()) {
            sessions.remove(key);
            return false;
        }
        if (!session.realName.equals(realName) || !session.idCard.equals(idCard.trim().toUpperCase())) {
            return false;
        }
        sessions.remove(key);
        return session.passed;
    }

    private String key(Long userId, String verifyNo) {
        return userId + ":" + verifyNo;
    }

    private static class Session {
        Long userId;
        String verifyNo;
        String realName;
        String idCard;
        boolean passed;
        long expireAt;
    }
}
