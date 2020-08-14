package com.lexiang.oauth;

import com.lexiang.oauth.properties.LoginProperties;
import com.lexiang.oauth.service.LoginService;
import com.lexiang.oauth.service.RedisService;
import com.lexiang.oauth.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(LoginProperties.class)
public class DefaultAutoConfiguration {

    @Autowired
    private LoginProperties loginProperties;

    @Autowired
    private RedisService redisServer;

    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public LoginService loginService(){
        return new LoginService(jwtUtils,redisServer,loginProperties);
    }

    @Bean
    public RedisService redisServer(){return new RedisService();}

    @Bean
    public JwtUtils jwtUtils(){return new JwtUtils();}



}
