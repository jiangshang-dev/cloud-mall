package org.jeecg.modules.cuisine.service.impl;

import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.cuisine.service.IRecipeVideoService;
import org.jeecg.modules.media.service.AliyunVodService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RecipeVideoServiceImpl implements IRecipeVideoService {

    @Resource
    private AliyunVodService aliyunVodService;

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
}
