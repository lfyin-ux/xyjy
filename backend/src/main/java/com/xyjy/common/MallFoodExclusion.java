package com.xyjy.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.entity.MallCategory;
import com.xyjy.entity.MallGoods;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商城食品类数据过滤 不展示零食饮品等商品
 */
public final class MallFoodExclusion {

    private static final String[] KEYWORDS = {"零食", "食品", "饮品", "饮料", "餐饮", "大礼包"};

    private MallFoodExclusion() {
    }

    public static boolean isFoodText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String keyword : KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFoodCategory(MallCategory category) {
        return category != null && isFoodText(category.getName());
    }

    public static boolean isFoodGoods(MallGoods goods) {
        if (goods == null) {
            return false;
        }
        return isFoodText(goods.getName()) || isFoodText(goods.getDetail());
    }

    public static List<MallCategory> filterCategories(List<MallCategory> categories) {
        return categories.stream()
                .filter(c -> !isFoodCategory(c))
                .collect(Collectors.toList());
    }

    public static List<MallGoods> filterGoods(List<MallGoods> goods) {
        return goods.stream()
                .filter(g -> !isFoodGoods(g))
                .collect(Collectors.toList());
    }

    public static void applyGoodsExclusion(LambdaQueryWrapper<MallGoods> wrapper) {
        for (String keyword : KEYWORDS) {
            wrapper.notLike(MallGoods::getName, keyword);
            wrapper.notLike(MallGoods::getDetail, keyword);
        }
    }

    public static void rejectFoodCategory(MallCategory category) {
        if (isFoodCategory(category)) {
            throw new BusinessException("商城不支持食品类分类");
        }
    }

    public static void rejectFoodGoods(MallGoods goods) {
        if (isFoodGoods(goods)) {
            throw new BusinessException("商城不支持食品类商品");
        }
    }
}
