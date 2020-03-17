package com.plm.project.system.controller;

import java.util.List;

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
import com.plm.common.utils.ServletUtils;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.security.LoginUser;
import com.plm.framework.security.service.TokenService;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.domain.ResultEntity;
import com.plm.project.system.domain.SysMenu;
import com.plm.project.system.service.ISysMenuService;

/**
 * 菜单信息
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {
    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private TokenService tokenService;

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@security.havePermission('system:menu:list')")
    @GetMapping("/list")
    public ResultEntity list(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return ResultEntity.success(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@security.havePermission('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public ResultEntity getInfo(@PathVariable Long menuId) {
        return ResultEntity.success(menuService.selectMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    public ResultEntity treeselect(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return ResultEntity.success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public ResultEntity roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        List<SysMenu> menus = menuService.selectMenuList(loginUser.getUser().getUserId());
        ResultEntity result = ResultEntity.success();
        result.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        result.put("menus", menuService.buildMenuTreeSelect(menus));
        return result;
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@security.havePermission('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return ResultEntity.error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        return result(menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@security.havePermission('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return ResultEntity.error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        return result(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@security.havePermission('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public ResultEntity remove(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return ResultEntity.error("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return ResultEntity.error("菜单已分配,不允许删除");
        }
        return result(menuService.deleteMenuById(menuId));
    }
}