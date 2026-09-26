package com.mall.web.controller.member.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdCheckinConfig;
import org.jeecg.modules.member.service.IMemberCheckinAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App签到")
@RestController
@RequestMapping("/member/checkin")
public class CheckinAppController {

    @Resource
    private IMemberCheckinAppService checkinAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "7日签到规则")
    @GetMapping("/config")
    public Result<List<FdCheckinConfig>> config() {
        return Result.OK(checkinAppService.listActiveConfig());
    }

    @Operation(summary = "今日签到状态")
    @GetMapping("/today")
    public Result<Map<String, Object>> today(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(checkinAppService.getTodayStatus(userId));
    }

    @Operation(summary = "执行签到")
    @PostMapping("/do")
    public Result<Map<String, Object>> doCheckin(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(checkinAppService.doCheckin(userId));
    }
}
