package com.plm.project.tool.gen.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.plm.framework.web.domain.ResultEntity;
import org.apache.commons.io.IOUtils;
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
import com.plm.common.utils.text.Convert;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.tool.gen.domain.GenTable;
import com.plm.project.tool.gen.domain.GenTableColumn;
import com.plm.project.tool.gen.service.IGenTableColumnService;
import com.plm.project.tool.gen.service.IGenTableService;

/**
 * 代码生成 操作处理
 *
 * @author plm
 */
@RestController
@RequestMapping("/tool/gen")
public class GenController extends BaseController {
    @Autowired
    private IGenTableService genTableService;

    @Autowired
    private IGenTableColumnService genTableColumnService;

    /**
     * 查询代码生成列表
     */
    @PreAuthorize("@security.havePermission('tool:gen:list')")
    @GetMapping("/list")
    public TableDataInfo genList(GenTable genTable) {
        startPage();
        List<GenTable> list = genTableService.selectGenTableList(genTable);
        return getDataTable(list);
    }

    /**
     * 修改代码生成业务
     */
    @PreAuthorize("@security.havePermission('tool:gen:query')")
    @GetMapping(value = "/{talbleId}")
    public ResultEntity getInfo(@PathVariable Long talbleId) {
        GenTable table = genTableService.selectGenTableById(talbleId);
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(talbleId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("info", table);
        map.put("rows", list);
        return ResultEntity.success(map);
    }

    /**
     * 查询数据库列表
     */
    @PreAuthorize("@security.havePermission('tool:gen:list')")
    @GetMapping("/db/list")
    public TableDataInfo dataList(GenTable genTable) {
        startPage();
        List<GenTable> list = genTableService.selectDbTableList(genTable);
        return getDataTable(list);
    }

    /**
     * 查询数据表字段列表
     */
    @PreAuthorize("@security.havePermission('tool:gen:list')")
    @GetMapping(value = "/column/{talbleId}")
    public TableDataInfo columnList(Long tableId) {
        TableDataInfo dataInfo = new TableDataInfo();
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
        dataInfo.setRows(list);
        dataInfo.setTotal(list.size());
        return dataInfo;
    }

    /**
     * 导入表结构（保存）
     */
    @PreAuthorize("@security.havePermission('tool:gen:list')")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    public ResultEntity importTableSave(String tables) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
        genTableService.importGenTable(tableList);
        return ResultEntity.success();
    }

    /**
     * 修改保存代码生成业务
     */
    @PreAuthorize("@security.havePermission('tool:gen:edit')")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity editSave(@Validated @RequestBody GenTable genTable) {
        System.out.println(genTable.getParams().size());
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return ResultEntity.success();
    }

    @PreAuthorize("@security.havePermission('tool:gen:remove')")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public ResultEntity remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return ResultEntity.success();
    }

    /**
     * 预览代码
     */
    @PreAuthorize("@security.havePermission('tool:gen:preview')")
    @GetMapping("/preview/{tableId}")
    public ResultEntity preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return ResultEntity.success(dataMap);
    }

    /**
     * 生成代码
     */
    @PreAuthorize("@security.havePermission('tool:gen:code')")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/genCode/{tableName}")
    public void genCode(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genTableService.generatorCode(tableName);
        genCode(response, data);
    }

    /**
     * 批量生成代码
     */
    @PreAuthorize("@security.havePermission('tool:gen:code')")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
        String[] tableNames = Convert.toStrArray(tables);
        byte[] data = genTableService.generatorCode(tableNames);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"platform.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}