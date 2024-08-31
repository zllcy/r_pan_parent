package com.imooc.pan.server.modules.share.service.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imooc.pan.server.common.cache.AbstractManualCacheService;
import com.imooc.pan.server.modules.share.entity.RPanShare;
import com.imooc.pan.server.modules.share.mapper.RPanShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 手动缓存实现分享业务的查询等操作
 */
@Component(value = "shareManualCacheService")
public class ShareCacheService extends AbstractManualCacheService<RPanShare> {

    @Autowired
    private RPanShareMapper mapper;

    @Override
    protected BaseMapper<RPanShare> getBaseMapper() {
        return mapper;
    }

    @Override
    public String getKeyFormat() {
        return "SHARE:ID:%s";
    }
}
