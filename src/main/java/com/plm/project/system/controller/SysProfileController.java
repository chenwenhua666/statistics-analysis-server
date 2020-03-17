package com.plm.project.system.controller;

import java.io.IOException;

import com.plm.framework.web.domain.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.plm.common.utils.SecurityUtils;
import com.plm.common.utils.ServletUtils;
import com.plm.common.utils.file.FileUploadUtils;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.config.PlatformConfig;
import com.plm.framework.security.LoginUser;
import com.plm.framework.security.service.TokenService;
import com.plm.framework.web.controller.BaseController;
import com.plm.project.system.domain.SysUser;
import com.plm.project.system.service.ISysUserService;

/**
 * 个人信息 业务处理
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public ResultEntity profile() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        ResultEntity result = ResultEntity.success(user);
        result.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        result.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
        return result;
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity updateProfile(@RequestBody SysUser user) {
        if (userService.updateUserProfile(user) > 0) {
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            // 更新缓存用户信息
            loginUser.getUser().setNickName(user.getNickName());
            loginUser.getUser().setPhonenumber(user.getPhonenumber());
            loginUser.getUser().setEmail(user.getEmail());
            loginUser.getUser().setSex(user.getSex());
            tokenService.setLoginUser(loginUser);
            return ResultEntity.success();
        }
        return ResultEntity.error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public ResultEntity updatePwd(String oldPassword, String newPassword) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        String userName = loginUser.getUsername();
        String password = loginUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password)) {
            return ResultEntity.error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            return ResultEntity.error("新密码不能与旧密码相同");
        }
        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(SecurityUtils.encryptPassword(newPassword));
            tokenService.setLoginUser(loginUser);
            return ResultEntity.success();
        }
        return ResultEntity.error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public ResultEntity avatar(@RequestParam("avatarfile") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            String avatar = FileUploadUtils.upload(PlatformConfig.getAvatarPath(), file);
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
                ResultEntity ajax = ResultEntity.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return ResultEntity.error("上传图片异常，请联系管理员");
    }
}
