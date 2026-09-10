package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.MallFoodExclusion;
import com.xyjy.common.Result;
import com.xyjy.entity.MallCategory;
import com.xyjy.entity.MallGoods;
import com.xyjy.entity.MallOrder;
import com.xyjy.entity.MallOrderItem;
import com.xyjy.mapper.MallCategoryMapper;
import com.xyjy.mapper.MallGoodsMapper;
import com.xyjy.mapper.MallOrderItemMapper;
import com.xyjy.mapper.MallOrderMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 商城与订单管理接口
 */
@RestController
@RequestMapping("/admin/mall")
public class AdminMallController {

    @Resource
    private MallGoodsMapper mallGoodsMapper;
    @Resource
    private MallCategoryMapper mallCategoryMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private MallOrderItemMapper mallOrderItemMapper;

    /**
     * 商品列表
     */
    @GetMapping("/goods/list")
    public Result<Page<MallGoods>> goodsList(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String keyword) {
        Page<MallGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MallGoods> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(MallGoods::getName, keyword);
        }
        MallFoodExclusion.applyGoodsExclusion(wrapper);
        wrapper.orderByDesc(MallGoods::getCreateTime);
        return Result.success(mallGoodsMapper.selectPage(page, wrapper));
    }

    /**
     * 新增或编辑商品
     */
    @PostMapping("/goods/save")
    public Result<Void> saveGoods(@RequestBody MallGoods goods) {
        if (goods.getName() == null || goods.getName().isEmpty()) {
            throw new BusinessException("请填写商品名称");
        }
        MallFoodExclusion.rejectFoodGoods(goods);
        if (goods.getId() == null) {
            if (goods.getStatus() == null) {
                goods.setStatus(1);
            }
            if (goods.getSales() == null) {
                goods.setSales(0);
            }
            mallGoodsMapper.insert(goods);
        } else {
            mallGoodsMapper.updateById(goods);
        }
        return Result.success();
    }

    /**
     * 上下架商品
     */
    @PostMapping("/goods/setStatus/{id}")
    public Result<Void> setGoodsStatus(@PathVariable Long id, @RequestParam Integer status) {
        MallGoods goods = mallGoodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goods.setStatus(status);
        mallGoodsMapper.updateById(goods);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/goods/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        mallGoodsMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 分类列表
     */
    @GetMapping("/categories")
    public Result<List<MallCategory>> categories() {
        List<MallCategory> list = mallCategoryMapper.selectList(new LambdaQueryWrapper<MallCategory>()
                .orderByAsc(MallCategory::getSort));
        return Result.success(MallFoodExclusion.filterCategories(list));
    }

    /**
     * 新增或编辑分类
     */
    @PostMapping("/category/save")
    public Result<Void> saveCategory(@RequestBody MallCategory category) {
        if (category.getName() == null || category.getName().isEmpty()) {
            throw new BusinessException("请填写分类名称");
        }
        MallFoodExclusion.rejectFoodCategory(category);
        if (category.getId() == null) {
            mallCategoryMapper.insert(category);
        } else {
            mallCategoryMapper.updateById(category);
        }
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        mallCategoryMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 订单列表
     */
    @GetMapping("/order/list")
    public Result<Page<Map<String, Object>>> orderList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) String orderNo) {
        Page<MallOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MallOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MallOrder::getStatus, status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(MallOrder::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(MallOrder::getCreateTime);
        Page<MallOrder> result = mallOrderMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("order", o);
            map.put("items", mallOrderItemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, o.getId())));
            return map;
        }).collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 发货 配送中
     */
    @PostMapping("/order/ship/{id}")
    public Result<Void> ship(@PathVariable Long id) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(3);
        mallOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 退款处理 pass同意退款
     */
    @PostMapping("/order/refund/{id}")
    public Result<Void> handleRefund(@PathVariable Long id, @RequestParam Boolean pass) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setRefundStatus(pass ? 2 : 3);
        mallOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 订单详情
     */
    @GetMapping("/order/detail/{id}")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long id) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("order", order);
        map.put("items", mallOrderItemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, id)));
        return Result.success(map);
    }
}
