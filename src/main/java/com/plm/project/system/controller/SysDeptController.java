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
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.web.controller.BaseController;
import com.plm.project.system.domain.SysDept;
import com.plm.project.system.service.ISysDeptService;

/**
 * 部门信息
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {
    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@security.havePermission('system:dept:list')")
    @GetMapping("/list")
    public ResultEntity list(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return ResultEntity.success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@security.havePermission('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public ResultEntity getInfo(@PathVariable Long deptId) {
        return ResultEntity.success(deptService.selectDeptById(deptId));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public ResultEntity treeselect(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return ResultEntity.success(deptService.buildDeptTreeSelect(depts));
    }

    /**
     * 加载对应角色部门列表树
     */
    @GetMapping(value = "/roleDeptTreeselect/{roleId}")
    public ResultEntity roleDeptTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        ResultEntity result = ResultEntity.success();
        result.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        result.put("depts", deptService.buildDeptTreeSelect(depts));
        return result;
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@security.havePermission('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysDept dept) {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            return ResultEntity.error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(SecurityUtils.getUsername());
        return result(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@security.havePermission('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysDept dept) {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            return ResultEntity.error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(dept.getDeptId())) {
            return ResultEntity.error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        dept.setUpdateBy(SecurityUtils.getUsername());
        return result(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@security.havePermission('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public ResultEntity remove(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return ResultEntity.error("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return ResultEntity.error("部门存在用户,不允许删除");
        }
        return result(deptService.deleteDeptById(deptId));
    }
}
