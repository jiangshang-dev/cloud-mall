package com.mall.web.controller.member.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdUserAddress;
import org.jeecg.modules.member.service.IAddressAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "App收货地址")
@RestController
@RequestMapping("/member/address")
public class AddressAppController {

    @Resource
    private IAddressAppService addressAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "地址列表")
    @GetMapping("/list")
    public Result<List<FdUserAddress>> list(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(addressAppService.listByUser(userId));
    }

    @Operation(summary = "新增/编辑地址")
    @PostMapping("/save")
    public Result<FdUserAddress> save(@RequestHeader("Authorization") String authorization,
                                      @RequestBody FdUserAddress address) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(addressAppService.saveAddress(userId, address));
    }

    @Operation(summary = "设为默认")
    @PutMapping("/default")
    public Result<String> setDefault(@RequestHeader("Authorization") String authorization,
                                     @RequestParam Long id) {
        Long userId = authHelper.resolveUserId(authorization);
        addressAppService.setDefault(userId, id);
        return Result.OK("设置成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestHeader("Authorization") String authorization,
                                 @RequestParam Long id) {
        Long userId = authHelper.resolveUserId(authorization);
        addressAppService.deleteAddress(userId, id);
        return Result.OK("删除成功");
    }
}
