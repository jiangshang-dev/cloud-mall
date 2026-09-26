package com.mall.web.controller.user.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.dto.EmailLoginDTO;
import org.jeecg.modules.user.dto.HuaweiLoginDTO;
import org.jeecg.modules.user.dto.PasswordLoginDTO;
import org.jeecg.modules.user.dto.ResetPasswordDTO;
import org.jeecg.modules.user.dto.SendEmailCodeDTO;
import org.jeecg.modules.user.dto.SendResetPasswordCodeDTO;
import org.jeecg.modules.user.dto.ThirdPartyLoginDTO;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.service.IVerificationCodeService;
import org.jeecg.modules.user.vo.LoginResultVO;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户登录")
@RestController
@RequestMapping("/user/auth")
public class UserLoginController {

    @Resource
    private IFdUserService userService;

    @Resource
    private IVerificationCodeService verificationCodeService;

    @GetMapping("/test")
    public Result<?> test() {
        return Result.ok("test");
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/sendEmailCode")
    public Result<?> sendEmailCode(@Validated @RequestBody SendEmailCodeDTO dto) {
        verificationCodeService.sendEmailCode(dto.getEmail(), dto.getScene());
        return Result.ok("验证码发送成功");
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/emailLogin")
    public Result<LoginResultVO> emailLogin(@Validated @RequestBody EmailLoginDTO loginDTO) {
        return Result.ok(userService.emailLogin(loginDTO));
    }

    @Operation(summary = "账号密码登录")
    @PostMapping({"/passwordLogin", "/login"})
    public Result<LoginResultVO> passwordLogin(@Validated @RequestBody PasswordLoginDTO loginDTO) {
        return Result.ok(userService.passwordLogin(loginDTO));
    }

    @Operation(summary = "发送重置密码验证码")
    @PostMapping("/sendResetPasswordCode")
    public Result<String> sendResetPasswordCode(@Validated @RequestBody SendResetPasswordCodeDTO dto) {
        userService.sendResetPasswordCode(dto);
        return Result.ok("验证码发送成功");
    }

    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.ok("密码重置成功");
    }

    @Operation(summary = "华为账号一键登录")
    @PostMapping("/huaweiLogin")
    public Result<LoginResultVO> huaweiLogin(@Validated @RequestBody HuaweiLoginDTO loginDTO) {
        return Result.ok(userService.huaweiLogin(loginDTO));
    }

    @Operation(summary = "第三方登录")
    @PostMapping("/thirdPartyLogin")
    public Result<LoginResultVO> thirdPartyLogin(@Validated @RequestBody ThirdPartyLoginDTO loginDTO) {
        return Result.ok(userService.thirdPartyLogin(loginDTO));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestHeader("Authorization") String authorization) {
        return Result.ok(userService.getUserInfo(authorization));
    }

    @Operation(summary = "绑定新的第三方账号")
    @PostMapping("/bind")
    public Result<?> bindThirdPartyAccount(@Validated @RequestBody ThirdPartyLoginDTO loginDTO) {
        return Result.ok();
    }

    @Operation(summary = "解绑第三方账号")
    @DeleteMapping("/unbind/{identityType}")
    public Result<?> unbindThirdPartyAccount(@PathVariable String identityType) {
        return Result.ok();
    }
}
