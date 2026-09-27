package com.mall.web.controller.cuisine.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.cuisine.service.IFdRecipeService;
import org.jeecg.modules.cuisine.service.IRecipeVideoService;
import org.jeecg.modules.cuisine.vo.RecipeAdminSaveDTO;
import org.jeecg.modules.cuisine.vo.RecipeDetailVO;
import org.jeecg.modules.cuisine.vo.RecipeListItemVO;
import org.jeecg.modules.member.service.IPointsTaskAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "菜谱")
@RestController
@RequestMapping("/cuisine/recipe")
public class RecipeController {

    private static final String PUBLISH_RECIPE_TASK = "PUBLISH_RECIPE";

    @Resource
    private IFdRecipeService recipeService;

    @Resource
    private IRecipeVideoService recipeVideoService;

    @Resource
    private IPointsTaskAppService pointsTaskAppService;

    @Resource
    private MemberAuthHelper memberAuthHelper;

    @Operation(summary = "新增菜谱")
    @PostMapping("/add")
    public Result<Map<String, String>> add(@RequestBody RecipeAdminSaveDTO recipe,
                                           @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long id = recipeService.saveRecipeAdmin(recipe, false);
        int rewardPoints = rewardPublishRecipe(authorization, id);
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(id));
        data.put("rewardPoints", String.valueOf(rewardPoints));
        return Result.ok(data);
    }

    private int rewardPublishRecipe(String authorization, Long recipeId) {
        if (authorization == null || authorization.isBlank()) {
            return 0;
        }
        try {
            Long userId = memberAuthHelper.resolveUserId(authorization);
            return pointsTaskAppService.tryReward(userId, PUBLISH_RECIPE_TASK, String.valueOf(recipeId));
        } catch (Exception e) {
            log.warn("发布菜谱积分发放失败 recipeId={}", recipeId, e);
            return 0;
        }
    }

    @Operation(summary = "菜谱分页列表")
    @GetMapping("/list")
    public Result<IPage<RecipeListItemVO>> list(@RequestParam(required = false) Long cuisineId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) Integer subType,
                                                @RequestParam(required = false) Long tagId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(recipeService.pageRecipes(cuisineId, categoryId, subType, tagId, keyword, pageNo, pageSize));
    }

    @Operation(summary = "解析阿里云VOD视频播放地址")
    @GetMapping("/video/play")
    public Result<Map<String, String>> videoPlay(@RequestParam String videoId) {
        return Result.ok(recipeVideoService.buildPlayInfo(videoId));
    }

    @Operation(summary = "菜谱详情")
    @GetMapping("/{recipeId}")
    public Result<RecipeDetailVO> detail(@PathVariable Long recipeId) {
        return Result.ok(recipeService.getRecipeDetail(recipeId));
    }

    @Operation(summary = "增加浏览量")
    @PostMapping("/{recipeId}/view")
    public Result<?> incrementView(@PathVariable Long recipeId) {
        recipeService.incrementViewCount(recipeId);
        return Result.ok("ok");
    }
}
