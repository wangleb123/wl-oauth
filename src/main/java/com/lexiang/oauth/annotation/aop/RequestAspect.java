package com.lexiang.oauth.annotation.aop;


import com.lexiang.oauth.WLUser;
import com.lexiang.oauth.adaptor.UserInfoAdaptor;
import com.lexiang.oauth.annotation.CheckUser;
import com.lexiang.oauth.service.LoginService;
import com.lexiang.utils.utils.JsonUtils;
import com.lexiang.utils.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author 王乐
 * @apiNote  注解处理
 */
@Aspect
@Component
@Slf4j
public class RequestAspect {
    private final static Logger logger = LoggerFactory.getLogger(RequestAspect.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private ApplicationContext applicationContext;


    /**
     * 表示在执行被@CheckUser注解修饰的方法之前 会执行doBefore()方法
     *
     * @param joinPoint 连接点，就是被拦截点
     */
    @Around(value = "@annotation(com.lexiang.oauth.annotation.CheckUser)")
    public Object doBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = RequestUtils.getRequest();
        UserInfoAdaptor bean = applicationContext.getBean(UserInfoAdaptor.class);
        //URL：根据请求对象拿到访问的地址
        log.info("url=" + request.getRequestURL());
        //获取请求的方法，是Get还是Post请求

        log.info("method=" + request.getMethod());
        //ip：获取到访问
        log.info("ip=" + request.getRemoteAddr());
        //伪异步

        //获取被拦截的类名和方法名
        log.info("class=" + joinPoint.getSignature().getDeclaringTypeName() +
                "and method name=" + joinPoint.getSignature().getName());
        //获取被拦截的类名和方法名
        log.info("class=" + joinPoint.getSignature().getDeclaringTypeName() +
                "and method name=" + joinPoint.getSignature().getName());
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        CheckUser annotation = method.getAnnotation(CheckUser.class);
        if(annotation.check()){
           loginService.checkToken();
        }
        loginService.setUser();
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if(arg instanceof WLUser){
                arg = JsonUtils.ObjectToBean(LoginService.getUser(), arg.getClass());
                bean.userHandler(request,method,annotation, arg);
            }
        }
        return joinPoint.proceed(args);
    }
}
