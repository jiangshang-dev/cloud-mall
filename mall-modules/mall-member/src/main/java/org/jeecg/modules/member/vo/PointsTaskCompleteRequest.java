package org.jeecg.modules.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "完成积分任务")
public class PointsTaskCompleteRequest {
    private String taskCode;
    private String bizRef;
}
