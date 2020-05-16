package com.zei.redis.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * redis read cache -> load db -> write cache
 * </p>
 *
 * @author zei
 * @since 2020年05月16日
 */
@Component
public class CacheTemplate {

    @Autowired
    RedisTemplate<String, String> redisTemplate;


    /**
     * 查询缓存通用方法，设置默认的过期时间
     *
     * @param key
     * @param clazz
     * @param loadDataService
     * @param <T>
     * @return
     */
    public <T> T loadCache(String key, Long expire, TypeReference<T> clazz, LoadDataService<T> loadDataService) {
        T result = loadCache(key, expire, TimeUnit.SECONDS, clazz, loadDataService);
        return result;
    }

    public <T> T loadCache(String key, long expire, TimeUnit unit, TypeReference<T> clazz, LoadDataService<T> loadDataService) {
        //先从缓存中查询
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        if (ops != null) {
            String jsonStr = String.valueOf(ops.get(key));
            if (!StringUtils.isEmpty(jsonStr) && !"null".equals(jsonStr)) {
                return JSON.parseObject(jsonStr, clazz);
            }
        }
//        //若不需防击穿
//        T result = loadDataService.loadData();
//        ops.set(key, JSON.toJSONString(result), expire, unit);
//        return result;

        //DCL
        //若缓存中没有，则去数据中获取
        //加锁，防止并发情况下 没有缓存时，数据库堵塞
        synchronized (this) {
            //先再从缓存中查看看
            if (ops != null) {
                String jsonStr = String.valueOf(ops.get(key));
                if (!StringUtils.isEmpty(jsonStr) && !"null".equals(jsonStr) && !"[]".equals(jsonStr)) {
                    return JSON.parseObject(jsonStr, clazz);
                }
            }
            //若确实是没有缓存的情况下， 则去自己的业务代码里查询
            T result = loadDataService.loadData();

            //将查处的结果放进缓存中
            ops.set(key, JSON.toJSONString(result), expire, unit);
            return result;
        }

    }
}
