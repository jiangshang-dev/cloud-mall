package com.mall.api.util;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;

/**
 * 演示期：未登录时回落到 demo 用户，便于先看效果。
 */
public final class MallUserUtils {

    public static final String DEMO_USER = "demo";

    private MallUserUtils() {
    }

    public static String currentUserId() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                LoginUser user = (LoginUser) principal;
                if (oConvertUtils.isNotEmpty(user.getId())) {
                    return user.getId();
                }
                if (oConvertUtils.isNotEmpty(user.getUsername())) {
                    return user.getUsername();
                }
            }
        } catch (Exception ignored) {
            // anon
        }
        return DEMO_USER;
    }

    public static String currentUsername() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                LoginUser user = (LoginUser) principal;
                if (oConvertUtils.isNotEmpty(user.getRealname())) {
                    return user.getRealname();
                }
                if (oConvertUtils.isNotEmpty(user.getUsername())) {
                    return user.getUsername();
                }
            }
        } catch (Exception ignored) {
            // anon
        }
        return "演示用户";
    }

    public static boolean isLoggedIn() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            return principal instanceof LoginUser;
        } catch (Exception e) {
            return false;
        }
    }
}
