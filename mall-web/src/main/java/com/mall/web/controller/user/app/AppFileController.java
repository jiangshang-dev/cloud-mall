package com.mall.web.controller.user.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.user.service.IAppFileService;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UploadFileVO;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

@Tag(name = "App文件")
@RestController
public class AppFileController {

    @Resource
    private IFdUserService userService;
    @Resource
    private IAppFileService appFileService;

    @Operation(summary = "App文件上传")
    @PostMapping("/user/upload")
    public Result<UploadFileVO> upload(@RequestHeader("Authorization") String authorization,
                                       HttpServletRequest request) {
        userService.getUserInfo(authorization);
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        String bizPath = request.getParameter("biz");
        return Result.ok(appFileService.upload(file, bizPath, request));
    }

    @GetMapping("/user/common/static/**")
    public void viewStatic(HttpServletRequest request, HttpServletResponse response) {
        String imgPath = extractPathFromPattern(request);
        appFileService.writeStaticFile(imgPath, response);
    }

    private String extractPathFromPattern(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
}
