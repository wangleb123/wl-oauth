package com.lexiang.oauth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wl-oauth")
public class LoginProperties {

    //是否需要登录
    private boolean enableLogin = false;

    //jwt key
    private String jwtKey = "yun-ye";

    //jwt秘钥文件
    private String JwtPublicKeyPath;

    //jwt公钥文件
    private String jwtPrivateKeyPath;

    //jwt有效时间
    private Long JWtTtlMillis = 60000L;

    //服务的key，只有在需要单点登录的时候才会使用
    private String serviceKey;

    //手机key
    private String phoneCodeKey;

    //存储用户到redis中的有效时间
    private long redisTtlMillis = 86400000;

    private boolean enableSSO = false;

}
