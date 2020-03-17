package com.plm.project.system.controller;

import java.util.List;
import java.util.Set;

import com.plm.framework.web.domain.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plm.common.constant.Constants;
import com.plm.common.utils.ServletUtils;
import com.plm.framework.security.LoginUser;
import com.plm.framework.security.service.SysLoginService;
import com.plm.framework.security.service.SysPermissionService;
import com.plm.framework.security.service.TokenService;
import com.plm.project.system.domain.SysMenu;
import com.plm.project.system.domain.SysUser;
import com.plm.project.system.service.ISysMenuService;

/**
 * 登录验证
 *
 * @author cwh
 */
@RestController
public class SysLoginController {
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录方法
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    @PostMapping("/login")
    public ResultEntity login(String username, String password, String code, String uuid) {
        ResultEntity result = ResultEntity.success();
        // 生成令牌
        String token = loginService.login(username, password, code, uuid);
        result.put(Constants.TOKEN, token);
        return result;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public ResultEntity getInfo() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        ResultEntity result = ResultEntity.success();
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", permissions);
        return result;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public ResultEntity getRouters() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        // 用户信息
        SysUser user = loginUser.getUser();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getUserId());
        return ResultEntity.success(menuService.buildMenus(menus));
    }
}
