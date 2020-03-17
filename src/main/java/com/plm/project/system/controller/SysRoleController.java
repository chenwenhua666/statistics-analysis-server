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
import com.plm.common.constant.UserConstants;
import com.plm.common.utils.SecurityUtils;
import com.plm.common.utils.poi.ExcelUtil;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.system.domain.SysRole;
import com.plm.project.system.service.ISysRoleService;

/**
 * 角色信息
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {
    @Autowired
    private ISysRoleService roleService;

    @PreAuthorize("@security.havePermission('system:role:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysRole role) {
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@security.havePermission('system:role:export')")
    @GetMapping("/export")
    public ResultEntity export(SysRole role) {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        return util.exportExcel(list, "角色数据");
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@security.havePermission('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public ResultEntity getInfo(@PathVariable Long roleId) {
        return ResultEntity.success(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@security.havePermission('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysRole role) {
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return ResultEntity.error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
            return ResultEntity.error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(SecurityUtils.getUsername());
        return result(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@security.havePermission('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return ResultEntity.error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
            return ResultEntity.error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(SecurityUtils.getUsername());
        return result(roleService.updateRole(role));
    }

    /**
     * 修改保存数据权限
     */
    @PreAuthorize("@security.havePermission('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public ResultEntity dataScope(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        return result(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@security.havePermission('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public ResultEntity changeStatus(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUsername());
        return result(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     */
    @PreAuthorize("@security.havePermission('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public ResultEntity remove(@PathVariable Long[] roleIds) {
        return result(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @PreAuthorize("@security.havePermission('system:role:query')")
    @GetMapping("/optionselect")
    public ResultEntity optionselect() {
        return ResultEntity.success(roleService.selectRoleAll());
    }
}