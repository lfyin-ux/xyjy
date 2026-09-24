package com.xyjy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyjy.common.BusinessException;
import com.xyjy.entity.SchoolInfo;
import com.xyjy.mapper.SchoolInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 学校字典服务：查找或创建标准学校记录
 */
@Service
public class SchoolInfoService {

    @Resource
    private SchoolInfoMapper schoolInfoMapper;

    /**
     * 按 ID 获取学校，不存在则抛异常
     */
    public SchoolInfo requireById(Long schoolId) {
        if (schoolId == null) {
            throw new BusinessException("请选择学校");
        }
        SchoolInfo info = schoolInfoMapper.selectById(schoolId);
        if (info == null) {
            throw new BusinessException("学校不存在");
        }
        return info;
    }

    /**
     * 按标准校名查找，不存在则创建
     */
    public SchoolInfo findOrCreate(String schoolName) {
        String name = normalizeName(schoolName);
        if (name.isEmpty()) {
            throw new BusinessException("请填写学校名称");
        }
        SchoolInfo exist = schoolInfoMapper.selectOne(new LambdaQueryWrapper<SchoolInfo>()
                .eq(SchoolInfo::getSchoolName, name).last("limit 1"));
        if (exist != null) {
            return exist;
        }
        SchoolInfo info = new SchoolInfo();
        info.setSchoolName(name);
        schoolInfoMapper.insert(info);
        return info;
    }

    /**
     * 优先使用已有 schoolId，否则按名称 findOrCreate
     */
    public SchoolInfo resolve(Long schoolId, String schoolName) {
        if (schoolId != null) {
            return requireById(schoolId);
        }
        return findOrCreate(schoolName);
    }

    private String normalizeName(String schoolName) {
        if (schoolName == null) {
            return "";
        }
        return schoolName.trim().replaceAll("\\s+", " ");
    }
}
