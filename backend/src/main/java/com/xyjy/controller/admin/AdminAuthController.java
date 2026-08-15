package com.xyjy.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.common.Result;
import com.xyjy.entity.SysAdmin;
import com.xyjy.entity.SysRole;
import com.xyjy.mapper.SysAdminMapper;
import com.xyjy.mapper.SysRoleMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理后台 登录与管理员管理接口
 */
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Resource
    private SysAdminMapper sysAdminMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<SysAdmin> login(@RequestParam String username, @RequestParam String password) {
        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username));
        if (admin == null || !admin.getPassword().equals(password)) {
            throw new BusinessException("账号或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException("账号已停用");
        }
        admin.setPassword(null);
        return Result.success(admin);
    }

    /**
     * 管理员列表
     */
    @GetMapping("/list")
    public Result<List<SysAdmin>> list() {
        List<SysAdmin> list = sysAdminMapper.selectList(null);
        list.forEach(a -> a.setPassword(null));
        return Result.success(list);
    }

    /**
     * 新增或编辑管理员
     */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody SysAdmin admin) {
        if (admin.getId() == null) {
            if (admin.getUsername() == null || admin.getUsername().isEmpty()) {
                throw new BusinessException("请填写账号");
            }
            admin.setStatus(1);
            sysAdminMapper.insert(admin);
        } else {
            sysAdminMapper.updateById(admin);
        }
        return Result.success();
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysAdminMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 角色列表
     */
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.success(sysRoleMapper.selectList(null));
    }
}
