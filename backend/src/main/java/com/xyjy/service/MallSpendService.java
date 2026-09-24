package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.MallOrder;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.MallOrderMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商城消费统计：用于跨校互动解锁
 */
@Service
public class MallSpendService {

    public static final BigDecimal CROSS_SCHOOL_UNLOCK_AMOUNT = new BigDecimal("2000");

    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private AppUserMapper appUserMapper;

    /**
     * 统计有效消费：已支付且未退款成功的订单
     */
    public BigDecimal calculateTotalSpent(Long userId) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        List<MallOrder> orders = mallOrderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getUserId, userId)
                .in(MallOrder::getStatus, 2, 3, 4)
                .and(w -> w.isNull(MallOrder::getRefundStatus).or().ne(MallOrder::getRefundStatus, 2)));
        BigDecimal total = BigDecimal.ZERO;
        for (MallOrder order : orders) {
            if (order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }

    public void refreshUserSpend(Long userId) {
        if (userId == null) {
            return;
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        user.setMallTotalSpent(calculateTotalSpent(userId));
        appUserMapper.updateById(user);
    }

    public boolean isCrossSchoolUnlocked(AppUser user) {
        BigDecimal spent = user.getMallTotalSpent() != null ? user.getMallTotalSpent() : BigDecimal.ZERO;
        return spent.compareTo(CROSS_SCHOOL_UNLOCK_AMOUNT) >= 0;
    }
}
