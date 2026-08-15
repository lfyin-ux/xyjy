package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.ErrandOrder;
import com.xyjy.mapper.ErrandOrderMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 校园跑腿接口
 */
@RestController
@RequestMapping("/errand")
public class ErrandController {

    @Resource
    private ErrandOrderMapper errandOrderMapper;

    /**
     * 可接订单列表 待接单状态
     */
    @GetMapping("/available")
    public Result<List<ErrandOrder>> available() {
        return Result.success(errandOrderMapper.selectList(new LambdaQueryWrapper<ErrandOrder>()
                .eq(ErrandOrder::getStatus, 1).orderByDesc(ErrandOrder::getCreateTime)));
    }

    /**
     * 订单详情
     */
    @GetMapping("/detail/{id}")
    public Result<ErrandOrder> detail(@PathVariable Long id) {
        ErrandOrder order = errandOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 发布跑腿订单
     */
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody ErrandOrder order) {
        if (order.getPublisherId() == null) {
            throw new BusinessException("缺少发单人");
        }
        if (order.getTitle() == null || order.getTitle().isEmpty()) {
            throw new BusinessException("请填写任务标题");
        }
        if (order.getFee() == null || order.getFee().doubleValue() <= 0) {
            throw new BusinessException("请设置赏金");
        }
        order.setStatus(1);
        errandOrderMapper.insert(order);
        return Result.success();
    }

    /**
     * 接单
     */
    @PostMapping("/accept")
    public Result<Void> accept(@RequestParam Long id, @RequestParam Long takerId) {
        ErrandOrder order = errandOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单已被接取或已取消");
        }
        order.setTakerId(takerId);
        order.setStatus(2);
        errandOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 更新订单状态 3已完成 4已取消
     */
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        ErrandOrder order = errandOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        errandOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 我发布的订单
     */
    @GetMapping("/my/publish/{userId}")
    public Result<List<ErrandOrder>> myPublish(@PathVariable Long userId,
                                               @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ErrandOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandOrder::getPublisherId, userId);
        if (status != null) {
            wrapper.eq(ErrandOrder::getStatus, status);
        }
        wrapper.orderByDesc(ErrandOrder::getCreateTime);
        return Result.success(errandOrderMapper.selectList(wrapper));
    }

    /**
     * 我接取的订单
     */
    @GetMapping("/my/take/{userId}")
    public Result<List<ErrandOrder>> myTake(@PathVariable Long userId,
                                            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ErrandOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandOrder::getTakerId, userId);
        if (status != null) {
            wrapper.eq(ErrandOrder::getStatus, status);
        }
        wrapper.orderByDesc(ErrandOrder::getCreateTime);
        return Result.success(errandOrderMapper.selectList(wrapper));
    }
}
