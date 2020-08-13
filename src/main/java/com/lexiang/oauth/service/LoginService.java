package com.lexiang.oauth.service;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Sets;
import com.lexiang.utils.enums.CodeEnum;
import com.lexiang.utils.exception.BusinessException;
import com.lexiang.utils.utils.AssetUtils;
import com.lexiang.utils.utils.TokenUtils;
import com.lexiang.oauth.WLUser;
import com.lexiang.oauth.properties.LoginProperties;
import com.lexiang.oauth.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class LoginService {


    private final RedisService redisService;

    private final JwtUtils jwtUtils;

    private final LoginProperties loginProperties;

    //threadLocal保存当前用户信息
    private static final ThreadLocal<WLUser>  userLocal = new ThreadLocal<>();

    public LoginService(JwtUtils jwtUtils, RedisService redisService, LoginProperties loginProperties){
        this.redisService = redisService;
        this.loginProperties = loginProperties;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 查看是否在yml填充用户规则白皮书的规则
     * @param loginProperties 白皮书配置
     */
    private void init(LoginProperties loginProperties){
        AssetUtils.assertObject(loginProperties,AssetUtils.assert_none, CodeEnum.USER_NOT_LOGIN);
    }


    /**
     * <>不需要从redis中获取用户的其他信息,且将用户信息存到UserUtil的threadLocal中</>
     */
    public void checkToken(){
        boolean exit = TokenUtils.isExit();
        //TODO
        if(exit){
            String token = TokenUtils.getToken();
            jwtUtils.parseJwt(token);
        }else {
            log.error("访问时请携带token");
            throw new BusinessException(CodeEnum.USER_NOT_LOGIN.getCode(),CodeEnum.USER_NOT_LOGIN.getName());
        }
    }

    public  void setUser(){
        String token = TokenUtils.getToken();
        if(!StringUtils.isEmpty(token)){
            Set<Object> keys = redisService.keys(token+":"+"*");
            if(keys.size() != 1){
                throw new BusinessException(CodeEnum.USER_NOT_LOGIN.getCode(),CodeEnum.USER_NOT_LOGIN.getName());
            }else {
                Object redisKey = keys.toArray()[0];
                WLUser WLUser = JSON.parseObject(JSON.toJSONString(redisService.get(redisKey.toString())), WLUser.class);
                userLocal.set(WLUser);
            }
        }
    }


    public static WLUser getUser() {
        return userLocal.get();
    }


    /**
     *
     * @param clams jwt参数
     * @param userKey 用户标示（作用在于是用户只能一处登录标示）
     * @param data 存储的用户数据
     * @return
     */
    public String login(Map<String, Object> clams ,Integer userKey,Object data){
        init(loginProperties);
        String serviceKey = loginProperties.getServiceKey();
        long redisTtlMillis = loginProperties.getRedisTtlMillis();
        Set<Object> isLogin = redisService.keys("*:"+serviceKey+userKey);
        if(loginProperties.isEnableSSO()){
            if(serviceKey == null || userKey == null){
                throw new BusinessException(405,"开启单点登录需配置serviceKey(yml配置)和用户唯一标示(登录时传入)");
            }
            if(isLogin != null){
                redisService.delete(isLogin);
            }
        }

        String jwtToken = jwtUtils.createJwtToken(clams);
        redisService.set(jwtToken+":"+serviceKey+userKey,data,redisTtlMillis);
        return jwtToken;
    }

    /**
     * @param extraKey 其他需要注销的数据
     */
    public void logout(String ...extraKey){
        String token = TokenUtils.getToken();
        Set<Object> isLogin = redisService.keys(token+":*");
        AssetUtils.assertObject(isLogin,AssetUtils.assert_none,CodeEnum.USER_NOT_LOGIN);
        redisService.delete(isLogin);
        if(extraKey != null){
            redisService.delete(Sets.newHashSet(extraKey));
        }
    }



    public static   <T> T getUserProperties(String userProperties,Class<T> clazz){
        return JSON.parseObject(userProperties,clazz);
    }


}
