package com.weblog.springbootstarter.aop;

import com.weblog.springbootstarter.config.WebLogProperties;
import com.weblog.springbootstarter.util.CusAccessObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * User: sunling
 * Date: 2024/7/23 13:58
 * Description: web log aspect
 **/
@EnableConfigurationProperties({WebLogProperties.class})
@Component
@Slf4j
@Aspect
public class WebLogAspect {

    @Resource
    private WebLogProperties webLogProperties;

    /**
     * all classes named *Controller
     */
    @Pointcut("execution(* *..*Controller.*(..))")
    public void webLog() {
    }

    /**
     * log before request
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        if (webLogProperties.isDisable()) {
            return;
        }

        //
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        writeAccessLog(request);
    }

    /**
     *
     * @return void
     * @throws
     * @author liupuxin
     * @since 2022/9/19 9:15
     */
    private void writeAccessLog(HttpServletRequest request) {
        String mediaType = request.getContentType();
        StringBuilder logs = new StringBuilder();
        logs.append("\n===> IP:  ").append(CusAccessObjectUtil.getIpAddress(request));
        logs.append("\n===> URL:  ").append(request.getRequestURL());
        logs.append("\n===> METHOD:  ").append(request.getMethod());
        logs.append("\n===> ContentType:  ").append(mediaType);
        logs.append("\n===> PARAM in url:   ").append(request.getQueryString());
        if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equalsIgnoreCase(mediaType) ||
                MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(mediaType)) {
            logs.append("\n===> PARAM in body:   ").append(getRequestBody(request));
        }
        log.info(logs.toString());
    }

    private String getRequestBody(HttpServletRequest request) {
        BufferedReader reader = null;
        try {
            StringBuilder jsonString = new StringBuilder();
            reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return jsonString.toString();
        } catch (Exception e) {
            log.error("get request body ex, url=" + request.getRequestURL(), e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return null;
    }

    /**
     * log after returning
     *
     * @param ret
     * @throws Throwable
     */
    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        if (webLogProperties.isDisable()) {
            return;
        }
        // 处理完请求，返回内容
        log.info("====> response : {}", ret);
    }

    /**
     * log after returning
     *
     * @param e
     * @throws Throwable
     */
    @AfterThrowing(throwing="e", pointcut = "webLog()")
    public void doAfterThrowing(Throwable e) {
        if (webLogProperties.isDisable()) {
            return;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 处理完请求，返回内容
        log.error("====> exception url=" + request.getRequestURL(), e);
    }
}
