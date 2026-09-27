package com.mall.common.util;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP地址
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019年01月14日
 */
public class IpUtils {
	private static Logger logger = LoggerFactory.getLogger(IpUtils.class);

	/**
	 * 获取IP地址
	 * 
	 * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
	 * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
	 */
	public static String getIpAddr(HttpServletRequest request) {
    	String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 ||CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
        	logger.error("IPUtils ERROR ", e);
        }

        //logger.info("获取客户端 ip：{} ", ip);
        // 使用代理，则获取第一个IP地址
        if (StringUtils.isNotEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                //ip = ip.substring(0, ip.indexOf(","));
                String[] ipAddresses = ip.split(",");
                for (String ipAddress : ipAddresses) {
                    ipAddress = ipAddress.trim();
                    if (isValidIpAddress(ipAddress)) {
                        return ipAddress;
                    }
                }
            }
        }
        
        return ip;
    }

    /**
     * 读取手机设备信息。优先使用客户端上报的品牌、型号、系统，没有时回退到 User-Agent。
     */
    public static String getDeviceInfo(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String brand = cleanHeader(request.getHeader("X-Device-Brand"));
        String model = cleanHeader(request.getHeader("X-Device-Model"));
        String os = cleanHeader(request.getHeader("X-Device-Os"));
        StringBuilder info = new StringBuilder();
        appendPart(info, brand);
        appendPart(info, model);
        appendPart(info, os);
        if (info.length() > 0) {
            return limit(info.toString(), 180);
        }
        return limit(cleanHeader(request.getHeader("User-Agent")), 180);
    }

    private static void appendPart(StringBuilder info, String part) {
        if (StringUtils.isEmpty(part)) {
            return;
        }
        if (info.length() > 0) {
            info.append(' ');
        }
        info.append(part);
    }

    private static String cleanHeader(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static String limit(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, max);
    }

    /**
     * 判断是否是IP格式
     * @param ipAddress
     * @return
     */
    public static boolean isValidIpAddress(String ipAddress) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
    
    /**
     * 获取服务器上的ip
     * @return
     */
    public static String getServerIp(){
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            //System.out.println("IP地址: " + ipAddress);
            return ipAddress;
        } catch (UnknownHostException e) {
            logger.error("获取ip地址失败", e);
        }
        return "";
    }
}
