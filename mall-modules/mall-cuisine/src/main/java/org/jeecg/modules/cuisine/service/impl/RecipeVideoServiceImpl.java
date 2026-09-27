package org.jeecg.modules.cuisine.service.impl;

import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.cuisine.service.IRecipeVideoService;
import org.jeecg.modules.media.entity.VideoInfoEntity;
import org.jeecg.modules.media.service.AliyunVodService;
import org.jeecg.modules.media.service.VideoInfoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RecipeVideoServiceImpl implements IRecipeVideoService {

    @Resource
    private AliyunVodService aliyunVodService;

    @Resource
    private VideoInfoService videoInfoService;

    @Override
    public boolean isHttpUrl(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return false;
        }
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    @Override
    public String resolvePlayUrl(String videoUrl) {
        if (oConvertUtils.isEmpty(videoUrl)) {
            return null;
        }
        if (isHttpUrl(videoUrl)) {
            return videoUrl;
        }
        try {
            GetPlayInfoResponse response = aliyunVodService.getPlayInfo(videoUrl.trim());
            if (response.getPlayInfoList() != null && !response.getPlayInfoList().isEmpty()) {
                return response.getPlayInfoList().getFirst().getPlayURL();
            }
        } catch (Exception e) {
            log.warn("解析阿里云 VOD 播放地址失败, videoId={}", videoUrl, e);
        }
        return null;
    }

    @Override
    public Map<String, String> buildPlayInfo(String videoId) {
        String playUrl = resolvePlayUrl(videoId);
        if (playUrl == null) {
            throw new JeecgBootException("视频暂不可播放，可能仍在转码中");
        }
        Map<String, String> data = new HashMap<>(2);
        data.put("videoId", videoId);
        data.put("playUrl", playUrl);
        return data;
    }

    @Override
    public Integer bindRecipeVideo(String videoUrl, String title, String description, String tags, Integer durationSeconds) {
        Integer duration = positive(durationSeconds);
        if (!isVodVideoId(videoUrl)) {
            return duration;
        }
        String videoId = videoUrl.trim();
        try {
            upsertVideoInfo(videoId, title, description, duration, null, null, null);
        } catch (Exception e) {
            log.warn("登记菜谱视频到 video_info 失败, videoId={}", videoId, e);
        }
        try {
            GetVideoInfoResponse response = aliyunVodService.getVideoInfo(videoId);
            GetVideoInfoResponse.Video video = response == null ? null : response.getVideo();
            if (video != null) {
                Integer vodDuration = video.getDuration() == null ? null : Math.round(video.getDuration());
                if (duration == null) {
                    duration = positive(vodDuration);
                }
                upsertVideoInfo(videoId, firstText(title, video.getTitle()), firstText(description, video.getDescription()),
                        duration != null ? duration : vodDuration, video.getCoverURL(), video.getStatus(), video.getSize());
            }
            if (oConvertUtils.isNotEmpty(title) || oConvertUtils.isNotEmpty(description) || oConvertUtils.isNotEmpty(tags)) {
                aliyunVodService.updateVideoInfo(videoId, title, description, tags);
            }
        } catch (Exception e) {
            log.warn("同步阿里云 VOD 视频信息失败, videoId={}", videoId, e);
        }
        return duration;
    }

    @Override
    public Integer lookupDuration(String videoUrl) {
        if (!isVodVideoId(videoUrl)) {
            return null;
        }
        try {
            VideoInfoEntity info = videoInfoService.getByVideoId(videoUrl.trim());
            return info == null ? null : positive(info.getDuration());
        } catch (Exception e) {
            log.warn("读取 video_info 时长失败, videoId={}", videoUrl, e);
            return null;
        }
    }

    private void upsertVideoInfo(String videoId, String title, String description, Integer duration,
                                 String coverUrl, String status, Long size) {
        VideoInfoEntity existing = videoInfoService.getByVideoId(videoId);
        VideoInfoEntity entity = existing == null ? new VideoInfoEntity() : existing;
        entity.setVideoId(videoId);
        if (oConvertUtils.isNotEmpty(title)) {
            entity.setTitle(title);
        }
        if (description != null) {
            entity.setDescription(description);
        }
        if (duration != null && duration > 0) {
            entity.setDuration(duration);
        }
        if (oConvertUtils.isNotEmpty(coverUrl)) {
            entity.setCoverUrl(coverUrl);
        }
        if (oConvertUtils.isNotEmpty(status)) {
            entity.setStatus(status);
        } else if (oConvertUtils.isEmpty(entity.getStatus())) {
            entity.setStatus("Normal");
        }
        if (size != null && size > 0) {
            entity.setSize(size);
        }
        if (existing == null) {
            videoInfoService.createVideoInfo(entity);
        } else {
            videoInfoService.updateByVideoId(entity);
        }
    }

    private boolean isVodVideoId(String value) {
        if (oConvertUtils.isEmpty(value) || isHttpUrl(value)) {
            return false;
        }
        String videoId = value.trim();
        if (videoId.contains("/") || videoId.contains("\\") || videoId.contains(".")) {
            return false;
        }
        return videoId.length() >= 8 && videoId.length() <= 64;
    }

    private Integer positive(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private String firstText(String preferred, String fallback) {
        return oConvertUtils.isNotEmpty(preferred) ? preferred : fallback;
    }
}
