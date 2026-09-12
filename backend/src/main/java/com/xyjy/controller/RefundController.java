package com.xyjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.MallOrder;
import com.xyjy.entity.MallOrderItem;
import com.xyjy.entity.RefundApply;
import com.xyjy.mapper.MallOrderItemMapper;
import com.xyjy.mapper.MallOrderMapper;
import com.xyjy.mapper.RefundApplyMapper;
import com.xyjy.service.WxPayService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款接口 支持单商品或部分退货
 */
@RestController
@RequestMapping("/refund")
public class RefundController {

    @Resource
    private RefundApplyMapper refundApplyMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private MallOrderItemMapper mallOrderItemMapper;
    @Resource
    private WxPayService wxPayService;

    /**
     * 用户提交退款申请 支持按订单明细选择退哪些商品和数量
     * 请求体示例：{ orderId:1, items:[{itemId:1, quantity:1, reason:"不想要了"}] }
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 订单必须是已支付以后的状态才能退款
        if (order.getStatus() < 2) {
            throw new BusinessException("待付款订单请直接取消");
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            throw new BusinessException("请选择要退款的商品");
        }

        for (Map<String, Object> item : items) {
            Long itemId = Long.valueOf(item.get("itemId").toString());
            int qty = item.get("quantity") != null ? Integer.parseInt(item.get("quantity").toString()) : 1;
            String reason = item.get("reason") != null ? item.get("reason").toString() : "";

            // 查询订单明细
            MallOrderItem orderItem = mallOrderItemMapper.selectById(itemId);
            if (orderItem == null) {
                throw new BusinessException("订单明细不存在");
            }
            if (qty > orderItem.getQuantity()) {
                throw new BusinessException(orderItem.getGoodsName() + "退货数量不能超过购买数量");
            }

            // 检查是否已申请过退款
            Long existCount = refundApplyMapper.selectCount(new LambdaQueryWrapper<RefundApply>()
                    .eq(RefundApply::getItemId, itemId)
                    .ne(RefundApply::getStatus, 2));
            if (existCount != null && existCount > 0) {
                throw new BusinessException(orderItem.getGoodsName() + "已有退款申请在处理中");
            }

            // 计算退款金额
            BigDecimal refundAmount = orderItem.getPrice().multiply(new BigDecimal(qty));

            RefundApply apply = new RefundApply();
            apply.setOrderId(orderId);
            apply.setOrderNo(order.getOrderNo());
            apply.setUserId(order.getUserId());
            apply.setItemId(itemId);
            apply.setGoodsName(orderItem.getGoodsName());
            apply.setSpec(orderItem.getSpec());
            apply.setQuantity(qty);
            apply.setRefundAmount(refundAmount);
            apply.setReason(reason);
            apply.setStatus(0);
            refundApplyMapper.insert(apply);
        }

        // 更新订单退款状态为申请中
        order.setRefundStatus(1);
        mallOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 用户查看自己的退款申请列表
     */
    @GetMapping("/my/{userId}")
    public Result<List<RefundApply>> myRefunds(@PathVariable Long userId) {
        return Result.success(refundApplyMapper.selectList(new LambdaQueryWrapper<RefundApply>()
                .eq(RefundApply::getUserId, userId).orderByDesc(RefundApply::getCreateTime)));
    }

    /**
     * 管理后台 退款申请列表
     */
    @GetMapping("/admin/list")
    public Result<Page<RefundApply>> adminList(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) Integer status) {
        Page<RefundApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RefundApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RefundApply::getStatus, status);
        }
        wrapper.orderByDesc(RefundApply::getCreateTime);
        return Result.success(refundApplyMapper.selectPage(page, wrapper));
    }

    /**
     * 管理后台 审核退款 同意或拒绝
     */
    @PostMapping("/admin/handle/{id}")
    public Result<Void> handle(@PathVariable Long id, @RequestParam Boolean pass,
                               @RequestParam(required = false) String rejectReason) {
        RefundApply apply = refundApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("退款申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }
        if (pass) {
            // 调用微信退款 退实际金额给用户
            MallOrder order = mallOrderMapper.selectById(apply.getOrderId());
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            int totalFen = order.getTotalAmount().multiply(new java.math.BigDecimal("100")).intValue();
            int refundFen = apply.getRefundAmount().multiply(new java.math.BigDecimal("100")).intValue();
            String refundNo = "RF" + apply.getId() + System.currentTimeMillis();
            boolean success = wxPayService.refund(order.getOrderNo(), refundNo, totalFen, refundFen);
            if (!success) {
                throw new BusinessException("退款接口调用失败，请稍后重试");
            }
            apply.setStatus(1);
            refundApplyMapper.updateById(apply);
            updateOrderRefundStatus(apply.getOrderId());
        } else {
            apply.setStatus(2);
            apply.setRejectReason(rejectReason);
            refundApplyMapper.updateById(apply);
            // 检查是否还有未处理的退款申请
            updateOrderRefundStatus(apply.getOrderId());
        }
        return Result.success();
    }

    /**
     * 查看某订单的退款详情
     */
    @GetMapping("/order/{orderId}")
    public Result<List<RefundApply>> orderRefunds(@PathVariable Long orderId) {
        return Result.success(refundApplyMapper.selectList(new LambdaQueryWrapper<RefundApply>()
                .eq(RefundApply::getOrderId, orderId).orderByAsc(RefundApply::getCreateTime)));
    }

    /**
     * 更新订单的退款状态 根据退款申请处理情况
     */
    private void updateOrderRefundStatus(Long orderId) {
        MallOrder order = mallOrderMapper.selectById(orderId);
        if (order == null) return;
        // 查询所有退款申请
        List<RefundApply> applies = refundApplyMapper.selectList(new LambdaQueryWrapper<RefundApply>()
                .eq(RefundApply::getOrderId, orderId));
        if (applies.isEmpty()) {
            order.setRefundStatus(0);
        } else {
            boolean allHandled = applies.stream().allMatch(a -> a.getStatus() != 0);
            boolean anyApproved = applies.stream().anyMatch(a -> a.getStatus() == 1);
            boolean anyPending = applies.stream().anyMatch(a -> a.getStatus() == 0);
            if (anyPending) {
                order.setRefundStatus(1);
            } else if (anyApproved) {
                order.setRefundStatus(2);
                // 已退款订单移入「退款售后」，不再出现在待备货等列表
                order.setStatus(5);
            } else {
                // 全部拒绝
                order.setRefundStatus(3);
            }
        }
        mallOrderMapper.updateById(order);
    }
}
