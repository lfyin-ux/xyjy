package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.ErrandOrder;
import com.xyjy.entity.SecondGoods;
import com.xyjy.mapper.ErrandOrderMapper;
import com.xyjy.mapper.SecondGoodsMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理后台 校园生活管理接口 跑腿与二手
 */
@RestController
@RequestMapping("/admin/life")
public class AdminLifeController {

    @Resource
    private ErrandOrderMapper errandOrderMapper;
    @Resource
    private SecondGoodsMapper secondGoodsMapper;

    /**
     * 跑腿订单列表
     */
    @GetMapping("/errand/list")
    public Result<Page<ErrandOrder>> errandList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) Integer status) {
        Page<ErrandOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ErrandOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ErrandOrder::getStatus, status);
        }
        wrapper.orderByDesc(ErrandOrder::getCreateTime);
        return Result.success(errandOrderMapper.selectPage(page, wrapper));
    }

    /**
     * 跑腿订单状态管理 含取消异常订单
     */
    @PostMapping("/errand/setStatus/{id}")
    public Result<Void> errandSetStatus(@PathVariable Long id, @RequestParam Integer status) {
        ErrandOrder order = errandOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        errandOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 二手商品列表
     */
    @GetMapping("/second/list")
    public Result<Page<SecondGoods>> secondList(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) Integer status) {
        Page<SecondGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SecondGoods> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SecondGoods::getStatus, status);
        }
        wrapper.orderByDesc(SecondGoods::getCreateTime);
        return Result.success(secondGoodsMapper.selectPage(page, wrapper));
    }

    /**
     * 二手商品审核通过
     */
    @PostMapping("/second/pass/{id}")
    public Result<Void> secondPass(@PathVariable Long id) {
        SecondGoods goods = secondGoodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goods.setStatus(1);
        secondGoodsMapper.updateById(goods);
        return Result.success();
    }

    /**
     * 二手商品下架或删除 status 2下架 5违规
     */
    @PostMapping("/second/setStatus/{id}")
    public Result<Void> secondSetStatus(@PathVariable Long id, @RequestParam Integer status) {
        SecondGoods goods = secondGoodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goods.setStatus(status);
        secondGoodsMapper.updateById(goods);
        return Result.success();
    }
}
