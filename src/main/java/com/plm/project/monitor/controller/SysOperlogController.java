package com.plm.project.monitor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plm.common.utils.poi.ExcelUtil;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.domain.ResultEntity;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.monitor.domain.SysOperLog;
import com.plm.project.monitor.service.ISysOperLogService;

/**
 * 操作日志记录
 *
 * @author plm
 */
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {
    @Autowired
    private ISysOperLogService operLogService;

    @PreAuthorize("@security.havePermission('monitor:operlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysOperLog operLog) {
        startPage();
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        return getDataTable(list);
    }

    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@security.havePermission('monitor:operlog:export')")
    @GetMapping("/export")
    public ResultEntity export(SysOperLog operLog) {
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
        return util.exportExcel(list, "操作日志");
    }

    @PreAuthorize("@security.havePermission('monitor:operlog:remove')")
    @DeleteMapping("/{operIds}")
    public ResultEntity remove(@PathVariable Long[] operIds) {
        return result(operLogService.deleteOperLogByIds(operIds));
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@security.havePermission('monitor:operlog:remove')")
    @DeleteMapping("/clean")
    public ResultEntity clean() {
        operLogService.cleanOperLog();
        return ResultEntity.success();
    }
}
