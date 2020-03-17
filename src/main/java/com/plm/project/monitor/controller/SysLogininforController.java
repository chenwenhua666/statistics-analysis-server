package com.plm.project.monitor.controller;

import java.util.List;

import com.plm.framework.web.domain.ResultEntity;
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
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.monitor.domain.SysLogininfor;
import com.plm.project.monitor.service.ISysLogininforService;

/**
 * 系统访问记录
 *
 * @author plm
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {
    @Autowired
    private ISysLogininforService logininforService;

    @PreAuthorize("@security.havePermission('monitor:logininfor:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor) {
        startPage();
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        return getDataTable(list);
    }

    @Log(title = "登陆日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@security.havePermission('monitor:logininfor:export')")
    @GetMapping("/export")
    public ResultEntity export(SysLogininfor logininfor) {
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        return util.exportExcel(list, "登陆日志");
    }

    @PreAuthorize("@security.havePermission('monitor:logininfor:remove')")
    @Log(title = "登陆日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public ResultEntity remove(@PathVariable Long[] infoIds) {
        return result(logininforService.deleteLogininforByIds(infoIds));
    }

    @PreAuthorize("@security.havePermission('monitor:logininfor:remove')")
    @Log(title = "登陆日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public ResultEntity clean() {
        logininforService.cleanLogininfor();
        return ResultEntity.success();
    }
}
