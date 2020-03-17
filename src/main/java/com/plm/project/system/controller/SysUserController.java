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
import org.springframework.web.multipart.MultipartFile;
import com.plm.common.constant.UserConstants;
import com.plm.common.utils.SecurityUtils;
import com.plm.common.utils.ServletUtils;
import com.plm.common.utils.StringUtils;
import com.plm.common.utils.poi.ExcelUtil;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.security.LoginUser;
import com.plm.framework.security.service.TokenService;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.system.domain.SysUser;
import com.plm.project.system.service.ISysPostService;
import com.plm.project.system.service.ISysRoleService;
import com.plm.project.system.service.ISysUserService;

/**
 * 用户信息
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private TokenService tokenService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@security.havePermission('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@security.havePermission('system:user:export')")
    @GetMapping("/export")
    public ResultEntity export(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@security.havePermission('system:user:import')")
    @PostMapping("/importData")
    public ResultEntity importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        String operName = loginUser.getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return ResultEntity.success(message);
    }

    @GetMapping("/importTemplate")
    public ResultEntity importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@security.havePermission('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    public ResultEntity getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        ResultEntity result = ResultEntity.success();
        result.put("roles", roleService.selectRoleAll());
        result.put("posts", postService.selectPostAll());
        if (StringUtils.isNotNull(userId)) {
            result.put(ResultEntity.DATA_TAG, userService.selectUserById(userId));
            result.put("postIds", postService.selectPostListByUserId(userId));
            result.put("roleIds", roleService.selectRoleListByUserId(userId));
        }
        return result;
    }

    /**
     * 新增用户
     */
    @PreAuthorize("@security.havePermission('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            return ResultEntity.error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return ResultEntity.error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return ResultEntity.error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(SecurityUtils.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return result(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@security.havePermission('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        if (UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return ResultEntity.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return ResultEntity.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        return result(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@security.havePermission('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public ResultEntity remove(@PathVariable Long[] userIds) {
        return result(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@security.havePermission('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public ResultEntity resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        return result(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@security.havePermission('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public ResultEntity changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setUpdateBy(SecurityUtils.getUsername());
        return result(userService.updateUserStatus(user));
    }
}