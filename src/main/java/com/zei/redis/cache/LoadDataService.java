package com.zei.redis.cache;


/**
 * <p>
 *
 * </p>
 *
 * @author zei
 * @since 2020年05月16日
 */
public interface LoadDataService<T> {

    <T> T loadData();
}
