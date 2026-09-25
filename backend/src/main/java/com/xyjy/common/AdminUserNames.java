package com.xyjy.common;

import com.alibaba.fastjson2.JSON;
import com.xyjy.entity.AppUser;
import com.xyjy.mapper.AppUserMapper;

import java.util.HashMap;
import java.util.Map;

public final class AdminUserNames {

    private AdminUserNames() {
    }

    public static String of(AppUserMapper mapper, Long userId) {
        if (userId == null) {
            return "-";
        }
        AppUser user = mapper.selectById(userId);
        if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
            return user.getNickname();
        }
        return "用户" + userId;
    }

    public static Map<String, Object> enrich(AppUserMapper mapper, Object entity, Long userId, String nameField) {
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(nameField, of(mapper, userId));
        return map;
    }
}
