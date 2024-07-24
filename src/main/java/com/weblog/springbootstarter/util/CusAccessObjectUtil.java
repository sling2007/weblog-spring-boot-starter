/**
 * 版权所有 (c) 华海智汇技术有限公司 2022-2022
 * Copyright (c) HMN Technologies Co., Ltd. 2022-2022. ALL rights reserved
 */

package com.weblog.springbootstarter.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
@Slf4j
public class CusAccessObjectUtil {

    private CusAccessObjectUtil(){
        //do nothing
    }

    private static final String IP_UTILS_FLAG = ",";
    private static final String IP_SEM_FLAG = ":";
    private static final String UNKNOWN = "unknown";
    private static final String BLANK = "";
    private static final String LOCALHOST_IP = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP1 = "127.0.0.1";

    /**
     * for java servlet, 获取IP HttpServletRequest
     *
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        try {
            //以下两个获取在k8s中，将真实的客户端IP，放到了x-Original-Forwarded-For。而将WAF的回源地址放到了 x-Forwarded-For了。
            ip = request.getHeader("X-Original-Forwarded-For");
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }
            //获取nginx等代理的ip
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("x-forwarded-for");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            //兼容k8s集群获取ip
            if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (LOCALHOST_IP1.equalsIgnoreCase(ip) || LOCALHOST_IP.equalsIgnoreCase(ip)) {
                    //根据网卡取本机配置的IP
                    InetAddress iNet = null;
                    try {
                        iNet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        log.error("getClientIp error: ", e);
                    }
                    if(iNet != null){
                        ip = iNet.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("IPUtils ERROR ", e);
        }
        //使用代理，则获取第一个IP地址
        if(!StringUtils.isEmpty(ip)){
            if(ip.indexOf(IP_SEM_FLAG) != -1){
                //适配前端  去除特殊符号
                ip = ip.substring(ip.lastIndexOf(":")+1);
            }
            if (ip.indexOf(IP_UTILS_FLAG) > 0) {
                ip = ip.substring(0, ip.indexOf(IP_UTILS_FLAG));
            }
        }
        log.info("portal-basic-api-CusAccessObjectUtil用户真实IP为：{}",ip);
        return ip;
    }


    /**
     * for spring 5.0, 获取用户真实IP地址 ServerHttpRequest
     *
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        String[] strings = new String[]{"Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "X-Real-IP"};
        if (ip != null && ip.length() != 0 && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if(ip.indexOf(IP_SEM_FLAG) != -1){
                ip = ip.substring(ip.lastIndexOf(":")+1);
            }
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        return getHeaderIp(ip, strings, request, headers);
    }


    /**
     * 获取header中携带的IP地址
     *
     */
    public static String getHeaderIp(String ip, String[] strings, ServerHttpRequest request, HttpHeaders headers) {
        for (String string : strings) {
            if (ip == null || ip.equals(BLANK) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = headers.getFirst(string);
            }
            if (ip != null && !ip.equals(BLANK)) {
                return ip;
            }

        }
        return request.getRemoteAddress().getAddress().getHostAddress();

    }
}
