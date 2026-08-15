package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.*;
import com.xyjy.mapper.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端 校园商城接口
 */
@RestController
@RequestMapping("/mall")
public class MallController {

    @Resource
    private MallCategoryMapper mallCategoryMapper;
    @Resource
    private MallGoodsMapper mallGoodsMapper;
    @Resource
    private MallCartMapper mallCartMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private MallOrderItemMapper mallOrderItemMapper;

    /**
     * 商品分类列表
     */
    @GetMapping("/categories")
    public Result<List<MallCategory>> categories() {
        return Result.success(mallCategoryMapper.selectList(new LambdaQueryWrapper<MallCategory>()
                .orderByAsc(MallCategory::getSort)));
    }

    /**
     * 商品列表 支持搜索和分类
     */
    @GetMapping("/goods")
    public Result<List<MallGoods>> goods(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<MallGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallGoods::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(MallGoods::getName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(MallGoods::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(MallGoods::getSales);
        return Result.success(mallGoodsMapper.selectList(wrapper));
    }

    /**
     * 商品详情
     */
    @GetMapping("/goods/{id}")
    public Result<MallGoods> goodsDetail(@PathVariable Long id) {
        MallGoods goods = mallGoodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return Result.success(goods);
    }

    /**
     * 购物车列表
     */
    @GetMapping("/cart/{userId}")
    public Result<List<Map<String, Object>>> cart(@PathVariable Long userId) {
        List<MallCart> list = mallCartMapper.selectList(new LambdaQueryWrapper<MallCart>()
                .eq(MallCart::getUserId, userId));
        List<Map<String, Object>> vos = list.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("cart", c);
            map.put("goods", mallGoodsMapper.selectById(c.getGoodsId()));
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 加入购物车
     */
    @PostMapping("/cart/add")
    public Result<Void> addCart(@RequestBody MallCart cart) {
        if (cart.getUserId() == null || cart.getGoodsId() == null) {
            throw new BusinessException("参数缺失");
        }
        if (cart.getQuantity() == null || cart.getQuantity() < 1) {
            cart.setQuantity(1);
        }
        mallCartMapper.insert(cart);
        return Result.success();
    }

    /**
     * 修改购物车数量
     */
    @PostMapping("/cart/update")
    public Result<Void> updateCart(@RequestParam Long id, @RequestParam Integer quantity) {
        MallCart cart = mallCartMapper.selectById(id);
        if (cart == null) {
            throw new BusinessException("购物车项不存在");
        }
        if (quantity < 1) {
            throw new BusinessException("数量不能小于1");
        }
        cart.setQuantity(quantity);
        mallCartMapper.updateById(cart);
        return Result.success();
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCart(@PathVariable Long id) {
        mallCartMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 提交订单
     */
    @PostMapping("/order/submit")
    @Transactional(rollbackFor = Exception.class)
    public Result<MallOrder> submitOrder(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            throw new BusinessException("请选择商品");
        }
        MallOrder order = new MallOrder();
        order.setOrderNo("DD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 1000));
        order.setUserId(userId);
        order.setStatus(1);
        order.setRefundStatus(0);
        order.setAddress(body.get("address") != null ? body.get("address").toString() : "");
        order.setReceiver(body.get("receiver") != null ? body.get("receiver").toString() : "");
        order.setPhone(body.get("phone") != null ? body.get("phone").toString() : "");
        BigDecimal total = BigDecimal.ZERO;
        mallOrderMapper.insert(order);
        for (Map<String, Object> item : items) {
            Long goodsId = Long.valueOf(item.get("goodsId").toString());
            int qty = item.get("quantity") != null ? Integer.parseInt(item.get("quantity").toString()) : 1;
            MallGoods goods = mallGoodsMapper.selectById(goodsId);
            if (goods == null) {
                throw new BusinessException("商品不存在");
            }
            MallOrderItem oi = new MallOrderItem();
            oi.setOrderId(order.getId());
            oi.setGoodsId(goodsId);
            oi.setGoodsName(goods.getName());
            oi.setGoodsCover(goods.getCover());
            oi.setSpec(item.get("spec") != null ? item.get("spec").toString() : "");
            oi.setPrice(goods.getPrice());
            oi.setQuantity(qty);
            mallOrderItemMapper.insert(oi);
            total = total.add(goods.getPrice().multiply(new BigDecimal(qty)));
        }
        order.setTotalAmount(total);
        mallOrderMapper.updateById(order);
        return Result.success(order);
    }

    /**
     * 微信支付 演示直接标记已支付
     */
    @PostMapping("/order/pay/{orderId}")
    public Result<Void> pay(@PathVariable Long orderId) {
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态异常");
        }
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        mallOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 我的订单 status为空查全部
     */
    @GetMapping("/orders/{userId}")
    public Result<List<Map<String, Object>>> orders(@PathVariable Long userId,
                                                    @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<MallOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallOrder::getUserId, userId);
        if (status != null) {
            wrapper.eq(MallOrder::getStatus, status);
        }
        wrapper.orderByDesc(MallOrder::getCreateTime);
        List<MallOrder> list = mallOrderMapper.selectList(wrapper);
        List<Map<String, Object>> vos = list.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("order", o);
            map.put("items", mallOrderItemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, o.getId())));
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/cancel/{orderId}")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order != null) {
            mallOrderMapper.deleteById(orderId);
        }
        return Result.success();
    }

    /**
     * 确认收货
     */
    @PostMapping("/order/confirm/{orderId}")
    public Result<Void> confirmOrder(@PathVariable Long orderId) {
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(4);
        mallOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 申请退款售后
     */
    @PostMapping("/order/refund/{orderId}")
    public Result<Void> refund(@PathVariable Long orderId) {
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(5);
        order.setRefundStatus(1);
        mallOrderMapper.updateById(order);
        return Result.success();
    }
}
