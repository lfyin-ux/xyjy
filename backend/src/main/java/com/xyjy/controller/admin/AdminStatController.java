package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.Result;
import com.xyjy.entity.MallOrder;
import com.xyjy.mapper.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 数据统计接口
 */
@RestController
@RequestMapping("/admin/stat")
public class AdminStatController {

    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private UserMatchMapper userMatchMapper;
    @Resource
    private ChatSessionMapper chatSessionMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private SquarePostMapper squarePostMapper;
    @Resource
    private PostLikeMapper postLikeMapper;
    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private ErrandOrderMapper errandOrderMapper;
    @Resource
    private SecondGoodsMapper secondGoodsMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private ReportRecordMapper reportRecordMapper;
    @Resource
    private ViolationRecordMapper violationRecordMapper;

    /**
     * 数据总览仪表盘
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> map = new HashMap<>();
        // 用户数
        map.put("totalUsers", appUserMapper.selectCount(null));
        // 匹配数
        Long matchCount = userMatchMapper.selectCount(new LambdaQueryWrapper<com.xyjy.entity.UserMatch>()
                .eq(com.xyjy.entity.UserMatch::getStatus, 1));
        map.put("matchCount", matchCount);
        // 会话与消息
        map.put("sessionCount", chatSessionMapper.selectCount(null));
        map.put("messageCount", chatMessageMapper.selectCount(null));
        // 广场
        map.put("postCount", squarePostMapper.selectCount(new LambdaQueryWrapper<com.xyjy.entity.SquarePost>()
                .eq(com.xyjy.entity.SquarePost::getStatus, 3)));
        map.put("likeCount", postLikeMapper.selectCount(null));
        map.put("commentCount", postCommentMapper.selectCount(null));
        // 跑腿
        map.put("errandTotal", errandOrderMapper.selectCount(null));
        map.put("errandFinished", errandOrderMapper.selectCount(new LambdaQueryWrapper<com.xyjy.entity.ErrandOrder>()
                .eq(com.xyjy.entity.ErrandOrder::getStatus, 3)));
        // 二手
        map.put("secondCount", secondGoodsMapper.selectCount(null));
        // 商城
        List<MallOrder> paidOrders = mallOrderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .ge(MallOrder::getStatus, 2).le(MallOrder::getStatus, 4));
        map.put("orderCount", mallOrderMapper.selectCount(null));
        BigDecimal gmv = paidOrders.stream().map(MallOrder::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        map.put("gmv", gmv);
        long refundCount = mallOrderMapper.selectCount(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getStatus, 5));
        map.put("refundCount", refundCount);
        // 举报违规封禁
        map.put("reportCount", reportRecordMapper.selectCount(null));
        map.put("violationCount", violationRecordMapper.selectCount(null));
        map.put("banCount", appUserMapper.selectCount(new LambdaQueryWrapper<com.xyjy.entity.AppUser>()
                .eq(com.xyjy.entity.AppUser::getStatus, 3)));
        return Result.success(map);
    }
}
