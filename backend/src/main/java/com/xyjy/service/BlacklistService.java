package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.entity.UserBlacklist;
import com.xyjy.mapper.UserBlacklistMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 黑名单服务 黑名单双向生效
 */
@Service
public class BlacklistService {

    @Resource
    private UserBlacklistMapper userBlacklistMapper;

    /**
     * 判断两个用户之间是否存在拉黑关系 任意一方拉黑对方都算
     */
    public boolean hasBlock(Long userId, Long otherId) {
        if (userId == null || otherId == null) {
            return false;
        }
        Long count = userBlacklistMapper.selectCount(new LambdaQueryWrapper<UserBlacklist>()
                .and(w -> w.eq(UserBlacklist::getUserId, userId).eq(UserBlacklist::getTargetId, otherId))
                .or(w -> w.eq(UserBlacklist::getUserId, otherId).eq(UserBlacklist::getTargetId, userId)));
        return count != null && count > 0;
    }
}
