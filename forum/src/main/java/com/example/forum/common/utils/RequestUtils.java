package com.example.forum.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {
    private RequestUtils(){}

    public static HttpServletRequest getCurrentRequest(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public static String getClientIp(){
        HttpServletRequest request = getCurrentRequest();

        if(request == null) return "Unknown";

        String ip = request.getHeader("X-Forwarded-For");
        if( ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }

        if(ip!= null && ip.contains(",")){
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    public static String getUserAgent(){
        HttpServletRequest request = getCurrentRequest();
        if(request == null) return "Unknown";

        String ua = request.getHeader("User-Agent");
        return (ua !=null && !ua.isBlank()) ? ua : "Unknown Device";
    }


}
