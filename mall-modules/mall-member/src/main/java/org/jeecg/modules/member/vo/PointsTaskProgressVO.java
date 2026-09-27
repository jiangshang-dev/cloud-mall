package org.jeecg.modules.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户积分任务进度")
public class PointsTaskProgressVO {

    private Long id;
    private String taskCode;
    private String title;
    private String description;
    private Integer rewardPoints;
    private String actionType;
    private Integer dailyLimit;
    private Integer totalLimit;
    private Integer todayCount;
    private Integer totalCount;
    private Boolean finished;
}
