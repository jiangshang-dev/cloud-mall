package com.mall.web.controller.cuisine.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.cuisine.entity.FdHomeBanner;
import org.jeecg.modules.cuisine.service.IFdHomeBannerService;
import org.jeecg.modules.cuisine.vo.HomeBannerAdminVO;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@Tag(name = "首页轮播图管理")
@RestController
@RequestMapping("/sys/fd/banner")
public class FdHomeBannerAdminController extends JeecgController<FdHomeBanner, IFdHomeBannerService> {

    @Operation(summary = "轮播图分页列表")
    @GetMapping("/list")
    public Result<IPage<HomeBannerAdminVO>> list(FdHomeBanner banner,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        return Result.OK(service.pageAdmin(banner, req.getParameterMap(), pageNo, pageSize));
    }

    @Operation(summary = "轮播图详情")
    @GetMapping("/queryById")
    public Result<FdHomeBanner> queryById(@RequestParam Long id) {
        return Result.OK(service.getById(id));
    }

    @AutoLog(value = "新增首页轮播图", operateType = CommonConstant.OPERATE_TYPE_2)
    @Operation(summary = "新增轮播图")
    @PostMapping("/add")
    public Result<String> add(@RequestBody FdHomeBanner banner) {
        service.applyDefaultsForSave(banner, true);
        service.save(banner);
        return Result.OK("添加成功");
    }

    @AutoLog(value = "编辑首页轮播图", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "编辑轮播图")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FdHomeBanner banner) {
        service.applyDefaultsForSave(banner, false);
        service.updateById(banner);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "删除首页轮播图", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "删除轮播图")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Long id) {
        service.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog(value = "批量删除首页轮播图", operateType = CommonConstant.OPERATE_TYPE_4)
    @Operation(summary = "批量删除轮播图")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}
