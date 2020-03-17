package com.plm.project.monitor.controller;

import com.plm.framework.web.domain.ResultEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.plm.framework.web.controller.BaseController;
import com.plm.framework.web.domain.Server;

/**
 * 服务器监控
 *
 * @author plm
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController extends BaseController {
    @PreAuthorize("@security.havePermission('monitor:server:list')")
    @GetMapping()
    public ResultEntity getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return ResultEntity.success(server);
    }
}
