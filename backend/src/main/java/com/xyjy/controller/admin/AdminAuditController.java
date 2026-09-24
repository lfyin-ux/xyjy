package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.AppUser;
import com.xyjy.entity.PersonalAuth;
import com.xyjy.entity.SchoolAuth;
import com.xyjy.entity.SchoolInfo;
import com.xyjy.mapper.AppUserMapper;
import com.xyjy.mapper.PersonalAuthMapper;
import com.xyjy.mapper.SchoolAuthMapper;
import com.xyjy.service.SchoolInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 认证审核接口
 */
@RestController
@RequestMapping("/admin/audit")
public class AdminAuditController {

    @Resource
    private PersonalAuthMapper personalAuthMapper;
    @Resource
    private SchoolAuthMapper schoolAuthMapper;
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private SchoolInfoService schoolInfoService;

    /**
     * 个人认证审核队列 默认审核中
     */
    @GetMapping("/personal/list")
    public Result<Page<Map<String, Object>>> personalList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                                          @RequestParam(required = false) Integer status) {
        Page<PersonalAuth> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PersonalAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalAuth::getStatus, status != null ? status : 1);
        wrapper.orderByDesc(PersonalAuth::getCreateTime);
        Page<PersonalAuth> result = personalAuthMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("auth", desensitize(a));
            map.put("user", appUserMapper.selectById(a.getUserId()));
            return map;
        }).collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 脱敏处理姓名和身份证号
     */
    private PersonalAuth desensitize(PersonalAuth a) {
        if (a.getRealName() != null && a.getRealName().length() > 1) {
            a.setRealName(a.getRealName().charAt(0) + "**");
        }
        if (a.getIdCard() != null && a.getIdCard().length() >= 8) {
            String id = a.getIdCard();
            a.setIdCard(id.substring(0, 4) + "**********" + id.substring(id.length() - 4));
        }
        return a;
    }

    /**
     * 个人认证审核通过
     */
    @PostMapping("/personal/pass/{id}")
    public Result<Void> personalPass(@PathVariable Long id) {
        PersonalAuth auth = personalAuthMapper.selectById(id);
        if (auth == null) {
            throw new BusinessException("认证记录不存在");
        }
        auth.setStatus(2);
        personalAuthMapper.updateById(auth);
        // 标记用户已实名
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setIdentityVerified(1);
            user.setPhone(auth.getPhone());
            appUserMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 个人认证审核驳回
     */
    @PostMapping("/personal/reject/{id}")
    public Result<Void> personalReject(@PathVariable Long id, @RequestParam String reason) {
        PersonalAuth auth = personalAuthMapper.selectById(id);
        if (auth == null) {
            throw new BusinessException("认证记录不存在");
        }
        auth.setStatus(3);
        auth.setRejectReason(reason);
        personalAuthMapper.updateById(auth);
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setIdentityVerified(0);
            appUserMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 学校认证审核队列
     */
    @GetMapping("/school/list")
    public Result<Page<Map<String, Object>>> schoolList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                        @RequestParam(required = false) Integer status) {
        Page<SchoolAuth> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SchoolAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolAuth::getStatus, status != null ? status : 1);
        wrapper.orderByDesc(SchoolAuth::getCreateTime);
        Page<SchoolAuth> result = schoolAuthMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("auth", a);
            map.put("user", appUserMapper.selectById(a.getUserId()));
            return map;
        }).collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 学校认证审核通过 绑定school_id
     */
    @PostMapping("/school/pass/{id}")
    public Result<Void> schoolPass(@PathVariable Long id,
                                   @RequestParam(required = false) Long schoolId,
                                   @RequestParam(required = false) String schoolName) {
        SchoolAuth auth = schoolAuthMapper.selectById(id);
        if (auth == null) {
            throw new BusinessException("认证记录不存在");
        }
        SchoolInfo school;
        if (schoolId != null) {
            school = schoolInfoService.requireById(schoolId);
        } else if (schoolName != null && !schoolName.trim().isEmpty()) {
            school = schoolInfoService.findOrCreate(schoolName);
        } else if (auth.getSchoolId() != null) {
            school = schoolInfoService.requireById(auth.getSchoolId());
        } else {
            school = schoolInfoService.findOrCreate(auth.getSchoolName());
        }
        auth.setStatus(2);
        auth.setSchoolId(school.getId());
        auth.setSchoolName(school.getSchoolName());
        schoolAuthMapper.updateById(auth);
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setSchoolVerified(1);
            user.setSchoolId(school.getId());
            user.setCurrentSchoolId(school.getId());
            user.setSchool(school.getSchoolName());
            user.setCollege(auth.getCollege());
            user.setGrade(auth.getGrade());
            user.setStudentNo(auth.getStudentNo());
            appUserMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 学校认证审核驳回
     */
    @PostMapping("/school/reject/{id}")
    public Result<Void> schoolReject(@PathVariable Long id, @RequestParam String reason) {
        SchoolAuth auth = schoolAuthMapper.selectById(id);
        if (auth == null) {
            throw new BusinessException("认证记录不存在");
        }
        auth.setStatus(3);
        auth.setRejectReason(reason);
        schoolAuthMapper.updateById(auth);
        AppUser user = appUserMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setSchoolVerified(0);
            appUserMapper.updateById(user);
        }
        return Result.success();
    }
}
