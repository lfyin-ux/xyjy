package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.FilterWord;
import com.xyjy.mapper.FilterWordMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理后台 过滤词库管理接口
 */
@RestController
@RequestMapping("/admin/filter")
public class AdminFilterController {

    @Resource
    private FilterWordMapper filterWordMapper;

    /**
     * 词库列表 wordType 1违规词 2审核词
     */
    @GetMapping("/list")
    public Result<Page<FilterWord>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Integer wordType,
                                         @RequestParam(required = false) String keyword) {
        Page<FilterWord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FilterWord> wrapper = new LambdaQueryWrapper<>();
        if (wordType != null) {
            wrapper.eq(FilterWord::getWordType, wordType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(FilterWord::getWord, keyword);
        }
        wrapper.orderByDesc(FilterWord::getCreateTime);
        return Result.success(filterWordMapper.selectPage(page, wrapper));
    }

    /**
     * 新增或编辑过滤词
     */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody FilterWord word) {
        if (word.getWord() == null || word.getWord().isEmpty()) {
            throw new BusinessException("请填写词语");
        }
        if (word.getId() == null) {
            if (word.getEnabled() == null) {
                word.setEnabled(1);
            }
            filterWordMapper.insert(word);
        } else {
            filterWordMapper.updateById(word);
        }
        return Result.success();
    }

    /**
     * 删除过滤词
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        filterWordMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 启用停用
     */
    @PostMapping("/toggle/{id}")
    public Result<Void> toggle(@PathVariable Long id, @RequestParam Integer enabled) {
        FilterWord word = filterWordMapper.selectById(id);
        if (word == null) {
            throw new BusinessException("词语不存在");
        }
        word.setEnabled(enabled);
        filterWordMapper.updateById(word);
        return Result.success();
    }

    /**
     * 批量导入
     */
    @PostMapping("/import")
    public Result<Void> batchImport(@RequestBody List<FilterWord> words) {
        for (FilterWord w : words) {
            if (w.getEnabled() == null) {
                w.setEnabled(1);
            }
            filterWordMapper.insert(w);
        }
        return Result.success();
    }

    /**
     * 导出全部词库
     */
    @GetMapping("/export")
    public Result<List<FilterWord>> export(@RequestParam(required = false) Integer wordType) {
        LambdaQueryWrapper<FilterWord> wrapper = new LambdaQueryWrapper<>();
        if (wordType != null) {
            wrapper.eq(FilterWord::getWordType, wordType);
        }
        return Result.success(filterWordMapper.selectList(wrapper));
    }
}
