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

    /**
     * 把菜谱视频登记到媒资 video_info，并用阿里云 VOD 补齐时长、封面和状态。
     * http 地址或本地路径不登记。媒资调用失败时返回入参时长，不抛给菜谱保存。
     *
     * @return 可用的时长秒数，没有则返回入参
     */
    Integer bindRecipeVideo(String videoUrl, String title, String description, String tags, Integer durationSeconds);

    /**
     * 从媒资 video_info 读取时长；没有记录时返回 null
     */
    Integer lookupDuration(String videoUrl);
}
