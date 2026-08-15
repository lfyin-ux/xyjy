package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.SecondGoods;
import com.xyjy.mapper.SecondGoodsMapper;
import com.xyjy.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 二手市场接口
 */
@RestController
@RequestMapping("/second")
public class SecondGoodsController {

    @Resource
    private SecondGoodsMapper secondGoodsMapper;
    @Resource
    private FilterService filterService;

    /**
     * 二手商品列表 支持搜索和分类
     */
    @GetMapping("/list")
    public Result<List<SecondGoods>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String category) {
        LambdaQueryWrapper<SecondGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecondGoods::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SecondGoods::getName, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(SecondGoods::getCategory, category);
        }
        wrapper.orderByDesc(SecondGoods::getCreateTime);
        return Result.success(secondGoodsMapper.selectList(wrapper));
    }

    /**
     * 商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<SecondGoods> detail(@PathVariable Long id) {
        SecondGoods goods = secondGoodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return Result.success(goods);
    }

    /**
     * 发布闲置商品 内容审核
     */
    @PostMapping("/publish")
    public Result<String> publish(@RequestBody SecondGoods goods) {
        if (goods.getSellerId() == null) {
            throw new BusinessException("缺少卖家");
        }
        if (goods.getName() == null || goods.getName().isEmpty()) {
            throw new BusinessException("请填写商品名称");
        }
        String checkText = goods.getName() + (goods.getDescription() == null ? "" : goods.getDescription());
        FilterService.FilterResult fr = filterService.check(checkText, "二手商品", goods.getSellerId());
        if (fr.level == 2) {
            return Result.error(fr.tip);
        }
        // 命中审核词进入待审核 否则直接上架
        goods.setStatus(fr.level == 1 ? 0 : 1);
        secondGoodsMapper.insert(goods);
        return Result.success(fr.level == 1 ? "商品已提交审核" : "发布成功");
    }

    /**
     * 编辑商品
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody SecondGoods goods) {
        if (goods.getId() == null) {
            throw new BusinessException("缺少商品ID");
        }
        secondGoodsMapper.updateById(goods);
        return Result.success();
    }

    /**
     * 下架商品
     */
    @PostMapping("/offline/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        SecondGoods goods = secondGoodsMapper.selectById(id);
        if (goods != null) {
            goods.setStatus(2);
            secondGoodsMapper.updateById(goods);
        }
        return Result.success();
    }

    /**
     * 我的二手商品
     */
    @GetMapping("/my/{userId}")
    public Result<List<SecondGoods>> my(@PathVariable Long userId) {
        return Result.success(secondGoodsMapper.selectList(new LambdaQueryWrapper<SecondGoods>()
                .eq(SecondGoods::getSellerId, userId).orderByDesc(SecondGoods::getCreateTime)));
    }
}
