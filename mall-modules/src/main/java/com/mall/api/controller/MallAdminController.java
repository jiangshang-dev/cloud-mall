package com.mall.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.api.dto.CdkBatchCreateRequest;
import com.mall.cdk.entity.MallCdk;
import com.mall.cdk.service.MallCdkService;
import com.mall.delivery.entity.MallDeliveryTask;
import com.mall.delivery.service.MallDeliveryService;
import com.mall.product.entity.MallProduct;
import com.mall.product.mapper.MallProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Tag(name = "ChatGPT-管理端-卡密商品")
@RestController
@RequestMapping("/mall/admin")
@RequiredArgsConstructor
public class MallAdminController {

    private final MallProductMapper productMapper;
    private final MallCdkService cdkService;
    private final MallDeliveryService deliveryService;

    @Operation(summary = "商品列表")
    @GetMapping("/product/list")
    public Result<List<MallProduct>> products() {
        return Result.OK(productMapper.selectList(new LambdaQueryWrapper<MallProduct>()
                .orderByAsc(MallProduct::getCreateTime)));
    }

    @Operation(summary = "保存商品")
    @PostMapping("/product/save")
    public Result<String> saveProduct(@RequestBody MallProduct product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setCreateTime(new Date());
            productMapper.insert(product);
        } else {
            product.setUpdateTime(new Date());
            productMapper.updateById(product);
        }
        return Result.OK("ok");
    }

    @Operation(summary = "批量生成卡密")
    @PostMapping("/cdk/batch")
    public Result<List<String>> batchCdk(@RequestBody CdkBatchCreateRequest req) {
        int count = req.getCount() == null ? 1 : req.getCount();
        int days = req.getExpireDays() == null ? 365 : req.getExpireDays();
        return Result.OK(cdkService.batchCreate(req.getProductId(), count, days));
    }

    @Operation(summary = "卡密列表")
    @GetMapping("/cdk/list")
    public Result<?> cdkList(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<MallCdk> q = new LambdaQueryWrapper<MallCdk>()
                .orderByDesc(MallCdk::getCreateTime);
        if (productId != null && !productId.isEmpty()) {
            q.eq(MallCdk::getProductId, productId);
        }
        if (status != null && !status.isEmpty()) {
            q.eq(MallCdk::getStatus, status);
        }
        return Result.OK(cdkService.page(new Page<>(pageNo, pageSize), q));
    }

    @Operation(summary = "交付任务列表")
    @GetMapping("/delivery/list")
    public Result<?> deliveryList(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize) {
        return Result.OK(deliveryService.page(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<MallDeliveryTask>().orderByDesc(MallDeliveryTask::getCreateTime)));
    }
}
