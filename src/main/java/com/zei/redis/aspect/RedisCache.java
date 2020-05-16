package com.zei.redis.aspect;

import java.lang.annotation.*;

/**
 * <p>
 * redis 缓存注解,支持el表达式
 * 注解使用方法：
 * 1、@RedisCache(key = "child:#{#id}")
 * 2、@RedisCache(key = "child:#{#id + '&'+ #name}")
 * 3、删除缓存 @RedisCache(key = "child:#{#id}", read = false)
 * </p>
 *
 * @author zei
 * @since 2020年05月16日
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache {

    /**
     * cache key
     *
     * @return
     */
    String key() default "";

    /**
     * 过期时间 默认1小时
     *
     * @return
     */
    long expired() default 60 * 60L;

    /**
     * 是否为查询数据
     * 若为新增、更新则做另外处理
     *
     * @return
     */
    boolean read() default true;
}
