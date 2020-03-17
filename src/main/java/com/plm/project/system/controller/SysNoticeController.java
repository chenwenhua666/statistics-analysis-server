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
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.system.domain.SysNotice;
import com.plm.project.system.service.ISysNoticeService;

/**
 * 公告 信息操作处理
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @PreAuthorize("@security.havePermission('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice) {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @PreAuthorize("@security.havePermission('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public ResultEntity getInfo(@PathVariable Long noticeId) {
        return ResultEntity.success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@security.havePermission('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysNotice notice) {
        notice.setCreateBy(SecurityUtils.getUsername());
        return result(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@security.havePermission('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysNotice notice) {
        notice.setUpdateBy(SecurityUtils.getUsername());
        return result(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@security.havePermission('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public ResultEntity remove(@PathVariable Long[] noticeIds) {
        return result(noticeService.deleteNoticeByIds(noticeIds));
    }
}
