package com.mall.web.controller.member.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.entity.FdPointsLedger;
import org.jeecg.modules.member.entity.FdPointsTask;
import org.jeecg.modules.member.service.IPointsAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.jeecg.modules.member.vo.PointsTaskCompleteRequest;
import org.jeecg.modules.member.vo.PointsTaskProgressVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App积分")
@RestController
@RequestMapping("/member/points")
public class PointsAppController {

    @Resource
    private IPointsAppService pointsAppService;
    @Resource
    private MemberAuthHelper authHelper;

    @Operation(summary = "积分账户概览")
    @GetMapping("/summary")
    public Result<FdPointsAccount> summary(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(pointsAppService.getAccountSummary(userId));
    }

    @Operation(summary = "积分流水")
    @GetMapping("/ledger")
    public Result<IPage<FdPointsLedger>> ledger(@RequestHeader("Authorization") String authorization,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(pointsAppService.pageLedger(userId, pageNo, pageSize));
    }

    @Operation(summary = "可完成任务列表")
    @GetMapping("/tasks")
    public Result<List<FdPointsTask>> tasks() {
        return Result.OK(pointsAppService.listActiveTasks());
    }

    @Operation(summary = "我的任务进度")
    @GetMapping("/tasks/mine")
    public Result<List<PointsTaskProgressVO>> myTasks(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(pointsAppService.listMyTasks(userId));
    }

    @Operation(summary = "完成任务领积分")
    @PostMapping("/task/complete")
    public Result<Map<String, Object>> completeTask(@RequestHeader("Authorization") String authorization,
                                                     @RequestBody PointsTaskCompleteRequest body) {
        Long userId = authHelper.resolveUserId(authorization);
        String taskCode = body == null ? null : body.getTaskCode();
        String bizRef = body == null ? null : body.getBizRef();
        return Result.OK(pointsAppService.completeTask(userId, taskCode, bizRef));
    }
}
