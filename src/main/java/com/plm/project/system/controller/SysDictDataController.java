package com.plm.project.system.controller;

import java.util.List;

import com.plm.framework.web.domain.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plm.common.utils.SecurityUtils;
import com.plm.common.utils.poi.ExcelUtil;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.system.domain.SysDictData;
import com.plm.project.system.service.ISysDictDataService;

/**
 * 数据字典信息
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController {
    @Autowired
    private ISysDictDataService dictDataService;

    @PreAuthorize("@security.havePermission('system:dict:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDictData dictData) {
        startPage();
        List<SysDictData> list = dictDataService.selectDictDataList(dictData);
        return getDataTable(list);
    }

    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @PreAuthorize("@security.havePermission('system:dict:export')")
    @GetMapping("/export")
    public ResultEntity export(SysDictData dictData) {
        List<SysDictData> list = dictDataService.selectDictDataList(dictData);
        ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
        return util.exportExcel(list, "字典数据");
    }

    /**
     * 查询字典数据详细
     */
    @PreAuthorize("@security.havePermission('system:dict:query')")
    @GetMapping(value = "/{dictCode}")
    public ResultEntity getInfo(@PathVariable Long dictCode) {
        return ResultEntity.success(dictDataService.selectDictDataById(dictCode));
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/dictType/{dictType}")
    public ResultEntity dictType(@PathVariable String dictType) {
        return ResultEntity.success(dictDataService.selectDictDataByType(dictType));
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@security.havePermission('system:dict:add')")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysDictData dict) {
        dict.setCreateBy(SecurityUtils.getUsername());
        return result(dictDataService.insertDictData(dict));
    }

    /**
     * 修改保存字典类型
     */
    @PreAuthorize("@security.havePermission('system:dict:edit')")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysDictData dict) {
        dict.setUpdateBy(SecurityUtils.getUsername());
        return result(dictDataService.updateDictData(dict));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@security.havePermission('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    public ResultEntity remove(@PathVariable Long[] dictCodes) {
        return result(dictDataService.deleteDictDataByIds(dictCodes));
    }
}
