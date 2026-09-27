package org.jeecg.modules.cuisine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.modules.cuisine.entity.FdRecipeIngredient;
import org.jeecg.modules.cuisine.entity.FdRecipeStep;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "管理端菜谱保存")
public class RecipeAdminSaveDTO {

    private Long id;
    private Long categoryId;
    private Long cuisineId;
    private String title;
    private String subtitle;
    private String description;
    private String coverImage;
    private String videoUrl;
    private Integer videoDuration;
    private Integer difficulty;
    private String skillLevel;
    private Integer cookMinutes;
    private String prepNote;
    private Integer calories;
    private Integer showCalories;
    private String servingSize;
    private String yieldCount;
    private Integer isRecommend;
    private Integer sortNo;
    private Integer status;
    private Integer privateOnly;

    @Schema(description = "食材列表")
    private List<FdRecipeIngredient> ingredients = new ArrayList<>();

    @Schema(description = "步骤列表")
    private List<FdRecipeStep> steps = new ArrayList<>();

    @Schema(description = "标签ID列表")
    private List<Long> tagIds = new ArrayList<>();

    @Schema(description = "标签名列表，不存在时自动创建")
    private List<String> tags = new ArrayList<>();
}
