package org.jeecg.modules.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.modules.user.vo.UploadFileVO;
import org.springframework.web.multipart.MultipartFile;

public interface IAppFileService {

    UploadFileVO upload(MultipartFile file, String bizPath, HttpServletRequest request);

    void writeStaticFile(String relativePath, HttpServletResponse response);
}
