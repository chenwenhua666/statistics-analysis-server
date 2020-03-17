package com.plm.project.monitor.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plm.common.constant.Constants;
import com.plm.common.utils.StringUtils;
import com.plm.framework.aspectj.lang.annotation.Log;
import com.plm.framework.aspectj.lang.enums.BusinessType;
import com.plm.framework.redis.RedisCache;
import com.plm.framework.security.LoginUser;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.domain.ResultEntity;
import com.plm.framework.web.page.TableDataInfo;
import com.plm.project.monitor.domain.SysUserOnline;
import com.plm.project.system.service.ISysUserOnlineService;

/**
 * 在线用户监控
 *
 * @author plm
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController {
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@security.havePermission('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName) {
        Collection<String> keys = redisCache.keys(Constants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys) {
            LoginUser user = redisCache.getCacheObject(key);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                if (StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername())) {
                    userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
                }
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                if (StringUtils.equals(ipaddr, user.getIpaddr())) {
                    userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
                }
            } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
                if (StringUtils.equals(userName, user.getUsername())) {
                    userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
                }
            } else {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@security.havePermission('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tokenId}")
    public ResultEntity forceLogout(@PathVariable String tokenId) {
        redisCache.deleteObject(Constants.LOGIN_TOKEN_KEY + tokenId);
        return ResultEntity.success();
    }
}
