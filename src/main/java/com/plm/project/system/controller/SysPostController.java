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
import com.plm.project.system.domain.SysPost;
import com.plm.project.system.service.ISysPostService;

/**
 * 岗位信息操作处理
 *
 * @author cwh
 */
@RestController
@RequestMapping("/system/post")
public class SysPostController extends BaseController {
    @Autowired
    private ISysPostService postService;

    /**
     * 获取岗位列表
     */
    @PreAuthorize("@security.havePermission('system:post:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysPost post) {
        startPage();
        List<SysPost> list = postService.selectPostList(post);
        return getDataTable(list);
    }

    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@security.havePermission('system:config:export')")
    @GetMapping("/export")
    public ResultEntity export(SysPost post) {
        List<SysPost> list = postService.selectPostList(post);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        return util.exportExcel(list, "岗位数据");
    }

    /**
     * 根据岗位编号获取详细信息
     */
    @PreAuthorize("@security.havePermission('system:post:query')")
    @GetMapping(value = "/{postId}")
    public ResultEntity getInfo(@PathVariable Long postId) {
        return ResultEntity.success(postService.selectPostById(postId));
    }

    /**
     * 新增岗位
     */
    @PreAuthorize("@security.havePermission('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultEntity add(@Validated @RequestBody SysPost post) {
        if (UserConstants.NOT_UNIQUE.equals(postService.checkPostNameUnique(post))) {
            return ResultEntity.error("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(postService.checkPostCodeUnique(post))) {
            return ResultEntity.error("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        post.setCreateBy(SecurityUtils.getUsername());
        return result(postService.insertPost(post));
    }

    /**
     * 修改岗位
     */
    @PreAuthorize("@security.havePermission('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultEntity edit(@Validated @RequestBody SysPost post) {
        if (UserConstants.NOT_UNIQUE.equals(postService.checkPostNameUnique(post))) {
            return ResultEntity.error("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(postService.checkPostCodeUnique(post))) {
            return ResultEntity.error("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        post.setUpdateBy(SecurityUtils.getUsername());
        return result(postService.updatePost(post));
    }

    /**
     * 删除岗位
     */
    @PreAuthorize("@security.havePermission('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public ResultEntity remove(@PathVariable Long[] postIds) {
        return result(postService.deletePostByIds(postIds));
    }

    /**
     * 获取岗位选择框列表
     */
    @GetMapping("/optionselect")
    public ResultEntity optionselect() {
        List<SysPost> posts = postService.selectPostAll();
        return ResultEntity.success(posts);
    }
}
