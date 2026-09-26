package org.jeecg.modules.interaction.util;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.stereotype.Component;

/**
 * 从 Authorization 请求头解析当前登录用户
 */
@Component
public class InteractionAuthHelper {

    @Resource
    private IFdUserService userService;

    public Long resolveUserId(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            throw new JeecgBootException("请先登录");
        }
        UserInfoVO info = userService.getUserInfo(authorization);
        if (info == null || info.getId() == null) {
            throw new JeecgBootException("请先登录");
        }
        return info.getId();
    }

    public Long resolveOptionalUserId(String authorization) {
        if (oConvertUtils.isEmpty(authorization)) {
            return null;
        }
        try {
            return resolveUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}
