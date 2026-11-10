package com.sqx.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.utils.TextGarbledUtils;
import com.sqx.common.mybatis.utils.PageUtils;
import com.sqx.common.mybatis.utils.Query;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.service.ISysDictService;
import com.sqx.system.service.SysDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("sysDictService")
public class SysDictServiceImpl implements SysDictService {
    @Autowired
    private ISysDictService sysDictService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String name = (String) params.get("name");
        String keyword = (String) params.get("keyword");
        String type = (String) params.get("type");
        String searchName = StringUtils.isNotBlank(name) ? name : keyword;

        IPage<SysDictEntity> page = sysDictService.page(
            new Query<SysDictEntity>().getPage(params),
            new QueryWrapper<SysDictEntity>()
                .like(StringUtils.isNotBlank(searchName), "name", searchName)
                .eq(StringUtils.isNotBlank(type), "type", type)
                .orderByAsc("order_num")
        );
        List<SysDictEntity> records = page.getRecords().stream()
                .filter(this::isValidDictRow)
                .collect(Collectors.toList());
        page.setRecords(records);

        return new PageUtils(page);
    }

    private boolean isValidDictRow(SysDictEntity dict) {
        if (dict == null) {
            return false;
        }
        return !TextGarbledUtils.hasGarbledText(dict.getName(), dict.getCode(), dict.getValue());
    }

}
