package com.mall.web.controller.member.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.entity.FdMallProduct;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.service.IMallAppService;
import org.jeecg.modules.member.service.IMallRedeemAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.jeecg.modules.member.vo.MallRedeemRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "App积分商城")
@RestController
@RequestMapping("/member/mall")
public class MallAppController {

    @Resource
    private IMallAppService mallAppService;
    @Resource
    private IMallRedeemAppService redeemAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "商城商品列表")
    @GetMapping("/products")
    public Result<List<FdMallProduct>> products() {
        return Result.OK(mallAppService.listActiveProducts());
    }

    @Operation(summary = "商品详情")
    @GetMapping("/product/detail")
    public Result<FdMallProduct> detail(@RequestParam Long id) {
        return Result.OK(mallAppService.getProductDetail(id));
    }

    @Operation(summary = "积分兑换")
    @PostMapping("/redeem")
    public Result<FdMallOrder> redeem(@RequestHeader("Authorization") String authorization,
                                      @RequestBody MallRedeemRequest body) {
        Long userId = authHelper.resolveUserId(authorization);
        if (body == null || oConvertUtils.isEmpty(body.getProductId())) {
            throw new JeecgBootException("请选择商品");
        }
        Long productId = Long.valueOf(body.getProductId());
        Long addressId = oConvertUtils.isEmpty(body.getAddressId()) ? null : Long.valueOf(body.getAddressId());
        return Result.OK(redeemAppService.redeem(userId, productId, body.getQuantity(), addressId));
    }

    @Operation(summary = "我的兑换订单")
    @GetMapping("/orders")
    public Result<IPage<FdMallOrder>> myOrders(@RequestHeader("Authorization") String authorization,
                                             @RequestParam(defaultValue = "1") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(mallAppService.pageUserOrders(userId, pageNo, pageSize));
    }
}
