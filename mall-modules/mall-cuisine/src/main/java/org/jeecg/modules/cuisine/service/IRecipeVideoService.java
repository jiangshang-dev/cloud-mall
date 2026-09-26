package org.jeecg.modules.cuisine.service;

import java.util.Map;

/**
 * 菜谱视频播放地址解析
 */
public interface IRecipeVideoService {

    /**
     * 将库中 videoUrl（可能是 VOD videoId 或 http 地址）解析为可播放 URL
     */
    String resolvePlayUrl(String videoUrl);

    boolean isHttpUrl(String value);

    /**
     * 解析 VOD 视频播放信息；不可播放时返回 null
     */
    Map<String, String> buildPlayInfo(String videoId);
}
