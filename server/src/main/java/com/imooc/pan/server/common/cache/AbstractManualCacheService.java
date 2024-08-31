package com.imooc.pan.server.common.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imooc.pan.cache.core.constants.CacheConstants;
import com.imooc.pan.core.exception.RPanBusinessException;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 手动处理缓存的公用顶级父类
 * @param <V>
 */
public abstract class AbstractManualCacheService<V> implements ManualCacheService<V> {

    @Autowired(required = false)
    private CacheManager cacheManager;

    private Object lock = new Object();

    protected abstract BaseMapper<V> getBaseMapper();

    @Override
    public V getById(Serializable id) {
        V result = getByCache(id);
        if (Objects.nonNull(result)) {
            return result;
        }
        // 使用锁机制避免缓存击穿问题
        synchronized (lock) {
            result = getByCache(id);
            if (Objects.nonNull(result)) {
                return result;
            }
            result = getByDB(id);
            if (Objects.nonNull(result)) {
                putCache(id, result);
            }
        }
        return result;
    }

    private void putCache(Serializable id, V entity) {
        String cacheKey = getCacheKey(id);
        Cache cache = getCache();
        if (Objects.isNull(cache)) {
            return;
        }
        if (Objects.isNull(entity)) {
            return;
        }
        cache.put(cacheKey, entity);
    }

    private V getByDB(Serializable id) {
        return getBaseMapper().selectById(id);
    }

    private V getByCache(Serializable id) {
        String cacheKey = getCacheKey(id);
        Cache cache = getCache();
        if (Objects.isNull(cache)) {
            return null;
        }
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        if (Objects.isNull(valueWrapper)) {
            return null;
        }
        return (V) valueWrapper.get();
    }

    private String getCacheKey(Serializable id) {
        return String.format(getKeyFormat(), id);
    }

    @Override
    public boolean updateById(Serializable id, V entity) {
        int rowNum = getBaseMapper().updateById(entity);
        removeCache(id);
        return rowNum == 1;
    }

    private void removeCache(Serializable id) {
        String cacheKey = getCacheKey(id);
        Cache cache = getCache();
        if (Objects.isNull(cache)) {
            return;
        }
        cache.evict(cacheKey);
    }

    @Override
    public boolean removeById(Serializable id) {
        int rowNum = getBaseMapper().deleteById(id);
        removeCache(id);
        return rowNum == 1;
    }

    @Override
    public List<V> getByIds(Collection<? extends Serializable> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return ids.stream().map(this::getById).collect(Collectors.toList());

    }

    @Override
    public boolean updateByIds(Map<? extends Serializable, V> entityMap) {
        if (MapUtils.isEmpty(entityMap)) {
            return true;
        }
        for (Map.Entry<? extends Serializable, V> entry : entityMap.entrySet()) {
            if (!updateById(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        for (Serializable id : ids) {
            if (!removeById(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Cache getCache() {
        if (Objects.isNull(cacheManager)) {
            throw new RPanBusinessException("the cache manager is empty!");
        }
        return cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
    }
}
