package com.lexiang.oauth.utils;


import com.lexiang.oauth.properties.LoginProperties;
import com.lexiang.utils.enums.CodeEnum;
import com.lexiang.utils.exception.BusinessException;
import com.lexiang.utils.utils.RequestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author 王乐
 * @apiNote  jwt工具
 */
@Slf4j
@Component
public class JwtUtils {

    @Autowired
    private LoginProperties loginProperties;


    /**
     * <>解析jwt的token</>
     * @param token header中的token
     * @return token解释出来的信息
     */
    public Claims parseJwt(String token){
        try {
            Claims claims = Jwts.parser()
                    //设置签名的秘钥
                    .setSigningKey(loginProperties.getJwtKey())
                    //设置需要解析的jwt
                    .parseClaimsJws(token).getBody();
            return claims;

        }catch (Exception e){
            log.error("token验证签名失败");
            throw new BusinessException(CodeEnum.USER_NOT_LOGIN.getCode(), CodeEnum.USER_NOT_LOGIN.getName());
        }

    }

    public  String createJwtToken(Map<String,Object> claims){
        //指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Long ttlMillis = loginProperties.getJWtTtlMillis();
        ttlMillis = ttlMillis*1000;
        //生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
        claims.put("ip", RequestUtils.getRequest().getRemoteAddr());
        //下面就是在为payload添加各种标准声明和私有声明了
        //这里其实就是new一个JwtBuilder，设置jwt的body
        JwtBuilder builder = Jwts.builder()
                //如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                //设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                .setId(UUID.randomUUID().toString())
                //iat: jwt的签发时间
                .setIssuedAt(now)
                //代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
                .setSubject("1234")
                //设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm,loginProperties.getJwtKey());
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            //设置过期时间
            builder.setExpiration(exp);
        }
        return builder.compact();

    }
}
