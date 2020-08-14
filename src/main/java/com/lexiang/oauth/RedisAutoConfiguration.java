
package com.lexiang.oauth;

import com.lexiang.oauth.service.RedisService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class RedisAutoConfiguration {

	// 将RedisTemplate默认的JDK序列化机制改编为json序列化机制

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
			throws UnknownHostException {
		// 創建RedisTemplate对象
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		// 新建Jackson的序列化机制
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
				Object.class);

		// 设置RedisTemplate的defaultSerializer为Jackson的序列化机制
		template.setDefaultSerializer(jackson2JsonRedisSerializer);
		template.setValueSerializer(jackson2JsonRedisSerializer);
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	@ConditionalOnBean(RedisTemplate.class)
	public RedisCacheManager redisCacheManager(RedisTemplate<Object, Object> redisTemplate) {
		// spring cache注解序列化配置

		// redisCache配置
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				// 将默认的key前缀去掉
				.disableKeyPrefix()

				// 设置key的序列化
				.serializeKeysWith((SerializationPair<String>) SerializationPair
						.fromSerializer(redisTemplate.getKeySerializer())) // key序列化方式

				// 设置value的序列化
				.serializeValuesWith(
						SerializationPair.fromSerializer(redisTemplate.getValueSerializer())) // value序列化方式

				// 不缓存null值
				.disableCachingNullValues()

				// 默认缓存过期时间
				.entryTtl(Duration.ofSeconds(60));

		// 设置一个初始化的缓存名称set集合
		Set<String> cacheNames = new HashSet<>();
		cacheNames.add("user");

		// 对每个缓存名称应用不同的配置,自定义过期时间
		Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
		configMap.put("user", redisCacheConfiguration.entryTtl(Duration.ofSeconds(120)));

		RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisTemplate.getConnectionFactory())

				.cacheDefaults(redisCacheConfiguration).transactionAware().initialCacheNames(cacheNames) // 注意这两句的调用顺序，一定要先调用该方法设置初始化的缓存名，再初始化相关的配置
				.withInitialCacheConfigurations(configMap)

				.build();
		return redisCacheManager;
	}



}

