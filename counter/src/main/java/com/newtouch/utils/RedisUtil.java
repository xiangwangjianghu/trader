package com.newtouch.utils;

import com.newtouch.enums.CacheEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Value("${cache-expire.captcha}")
    private int captchaExpireTime;

    @Value("${cache-expire.account}")
    private int accountExpireTime;

    @Value("${cache-expire.order}")
    private int orderExpireTime;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 增加缓存
    public void addCache(String key, String value, CacheEnum cacheEnum) {
        int expireTime = switch (cacheEnum) {
            case ACCOUNT -> accountExpireTime;
            case CAPTCHA -> captchaExpireTime;
            case ORDER, TRADE, POSI -> orderExpireTime;
            default -> 10;
        };

        stringRedisTemplate.opsForValue().set(cacheEnum.getType() + key, value, expireTime, TimeUnit.SECONDS);
    }

    // 查询缓存
    public String getCache(String key, CacheEnum cacheEnum) {
        return stringRedisTemplate.opsForValue().get(cacheEnum.getType() + key);
    }

    // 删除缓存
    public void deleteCache(String key, CacheEnum cacheEnum) {
        stringRedisTemplate.delete(cacheEnum.getType() + key);
    }
}
