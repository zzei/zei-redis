package com.zei.redis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author zei
 * @since 2020年05月16日
 */
@Component
public class RedisUtil {

    private static final String LIST_DELETE_VALUE = "listDeleteValue";

    private static final String LOCK_KEY = "redisLockKey:";

    private static final Long LOCK_KEY_EXPIRATION = 30L;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    private RedissonClient redissonClient;

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 给key值设置过期时间 单位秒
     * @param key
     * @param expiration
     */
    public void expire(String key, Long expiration) {
        redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
    }

    /**
     * 删除key
     * @param key
     */
    public Boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除key
     * @param key
     */
    public Boolean deleteKeys(String ...key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                return redisTemplate.delete(key);
            } else {
                return redisTemplate.delete(CollectionUtils.arrayToList(key)) == key.length;
            }
        }
        return false;
    }

    /**
     * 获取key的过期时间 单位秒
     * @param key
     * @return
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * string set
     * @param key
     * @param object
     */
    public void set(String key, Object object) {
        redisTemplate.opsForValue().set(key, object);
    }

    /**
     * string set 超时单位为妙
     * @param key
     * @param object
     * @param expiration
     */
    public void set(String key, Object object, Long expiration) {
        redisTemplate.opsForValue().set(key, object, expiration, TimeUnit.SECONDS);
    }

    /**
     * string set 自定义单位
     * @param key
     * @param object
     * @param expiration
     * @param timeUnit
     */
    public void set(String key, Object object, Long expiration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, object, expiration, timeUnit);
    }

    /**
     * string 自增1
     * @param key
     * @return
     */
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key, 1);
    }

    /**
     * string 自增 num 可为负
     * @param key
     * @param num
     * @return
     */
    public Long incr(String key, Long num) {
        return redisTemplate.opsForValue().increment(key, num);
    }

    /**
     * string get
     * @param key
     * @return
     */
    public Object get(String key) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        if (valueOperations != null) {
            return valueOperations.get(key);
        }
        return null;
    }

    /**
     * hash set
     * @param key
     * @param map
     */
    public void setHash(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * hash set 过期时间单位为秒
     * @param key
     * @param map
     * @param expiration
     */
    public void setHash(String key, Map<String, Object> map, Long expiration) {
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
    }

    /**
     * hash 获取map
     * @param key
     * @return
     */
    public Map<String, Object> getMHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * hash 获取value
     * @param key
     * @param field
     * @return
     */
    public Object getVHash(String key, String field) {
        return redisTemplate.opsForHash().entries(key).get(field);
    }

    /**
     * hash 删除hash表的内容
     * @param key
     * @param field
     * @return
     */
    public Boolean deleteHash(String key, Object ...field) {
        return redisTemplate.opsForHash().delete(key, field) == field.length;
    }

    /**
     * hash 自增1
     * @param key
     * @param field
     * @return
     */
    public Long hIncr(String key, String field) {
        return redisTemplate.opsForHash().increment(key, field, 1);
    }

    /**
     * hash 自增num 可为负
     * @param key
     * @param field
     * @param num
     * @return
     */
    public Long hIncr(String key, String field, Long num) {
        return redisTemplate.opsForHash().increment(key, field, num);
    }

    /**
     * list 获取列表
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<Object> lGet(String key, Long start, Long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * list 获取列表长度
     * @param key
     * @return
     */
    public long lGetLength(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * list 获取index的值
     * @param key
     * @param index
     * @return
     */
    public Object lGetByIndex(String key, Long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * list 往list添加一条记录
     * @param key
     * @param value
     */
    public void lSet(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * list 往list添加一条记录 带过期时间 单位秒
     * @param key
     * @param value
     * @param expiration
     */
    public void lSet(String key, Object value, Long expiration) {
        redisTemplate.opsForList().rightPush(key, value);
        redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
    }

    /**
     * list 往list添加集合
     * @param key
     * @param value
     */
    public void lSetList(String key, List<Object> value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * list 往list添加集合 带过期时间 单位秒
     * @param key
     * @param value
     * @param expiration
     */
    public void lSetList(String key, List<Object> value, Long expiration) {
        redisTemplate.opsForList().rightPushAll(key, value);
        redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
    }

    /**
     * list 修改index的值
     * @param key
     * @param index
     * @param value
     */
    public void lUpdateIndex(String key, Long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * list 删除index
     * @param key
     * @param index
     */
    public synchronized void lRemoveIndex(String key, Long index) {
        redisTemplate.opsForList().set(key, index, LIST_DELETE_VALUE);
        redisTemplate.opsForList().remove(key, 0, LIST_DELETE_VALUE);
    }

    /**
     * list 批量删除index
     * @param key
     * @param indexs
     */
    public synchronized void lRemoveIndexAll(String key, List<Long> indexs) {
        for (Long index : indexs) {
            redisTemplate.opsForList().set(key, index, LIST_DELETE_VALUE);
        }
        redisTemplate.opsForList().remove(key, 0, LIST_DELETE_VALUE);
    }

//    /**
//     * 分布式加锁 强制过期时间为30秒
//     * @param lockKey
//     * @return
//     */
//    public Boolean lock(String lockKey) {
//        try {
//            String key = LOCK_KEY + lockKey;
//            RLock myLock = redissonClient.getFairLock(key);
//            myLock.lock(LOCK_KEY_EXPIRATION, TimeUnit.SECONDS);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    /**
//     * 分布式释放锁
//     * @param lockKey
//     * @return
//     */
//    public Boolean unLock(String lockKey) {
//        String key = LOCK_KEY + lockKey;
//        RLock myLock = redissonClient.getFairLock(key);
//        myLock.unlock();
//        return true;
//    }

}
