package com.sqx.system.controller;


import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.core.utils.TextGarbledUtils;
import com.sqx.common.core.validator.ValidatorUtils;
import com.sqx.common.mybatis.utils.PageUtils;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.service.ISysDictService;
import com.sqx.system.domain.res.PageRes;
import com.sqx.system.domain.res.SysDictRes;
import com.sqx.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 数据字典
 *
 */
@Tag(name = "数据字典", description = "数据字典")
@RestController
@RequestMapping("sys/dict")
public class SysDictController {
    @Autowired
    private SysDictService sysDictService;
    @Autowired
    private ISysDictService iSysDictService;

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @RequiresPermissions("sys:dict:list")
    public Result<PageRes> list(@RequestParam Map<String, Object> params){
        PageUtils page = sysDictService.queryPage(params);

        PageRes res = new PageRes();
        res.setPage(page);
        return Result.success(res);
    }


    /**
     * 信息
     */
    @Operation(summary = "信息")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @RequiresPermissions("sys:dict:info")
    public Result<SysDictEntity> info(@PathVariable("id") Long id){
        SysDictEntity dict = iSysDictService.getById(id);

        SysDictRes res = new SysDictRes();
        res.setDict(dict);
        return Result.success(dict);
    }

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @RequiresPermissions(value = {"sys:dict:save", "sys:dict:list"}, logical = Logical.OR)
    public Result<Void> save(@RequestBody SysDictEntity dict){
        //校验类型
        ValidatorUtils.validateEntity(dict);
        validateDictText(dict);

        iSysDictService.save(dict);

        return Result.success();
    }

    /**
     * 修改
     */
    @Operation(summary = "修改")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @RequiresPermissions(value = {"sys:dict:update", "sys:dict:list"}, logical = Logical.OR)
    public Result<Void> update(@RequestBody SysDictEntity dict){
        //校验类型
        ValidatorUtils.validateEntity(dict);
        validateDictText(dict);

        iSysDictService.updateById(dict);

        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @RequiresPermissions(value = {"sys:dict:delete", "sys:dict:list"}, logical = Logical.OR)
    public Result<Void> delete(@RequestBody Long[] ids){
        iSysDictService.removeByIds(Arrays.asList(ids));

        return Result.success();
    }

    private void validateDictText(SysDictEntity dict) {
        if (TextGarbledUtils.hasGarbledText(dict.getName(), dict.getCode(), dict.getValue())) {
            throw new SqxException("字典名称、编码或值不能为乱码或仅包含问号");
        }
    }

}
