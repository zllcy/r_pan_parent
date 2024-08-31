package com.imooc.pan.bloom.filter.local;

import com.google.common.collect.Maps;
import com.imooc.pan.bloom.filter.core.BloomFilter;
import com.imooc.pan.bloom.filter.core.BloomFilterManager;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 本地布隆过滤器的管理器
 */
@Component
public class LocalBloomFilterManager implements BloomFilterManager, InitializingBean {

    @Autowired
    private LocalBloomFilterConfig config;

    private final Map<String, BloomFilter> bloomFilterContainer = Maps.newConcurrentMap();

    @Override
    public BloomFilter getFilter(String name) {
        return bloomFilterContainer.get(name);
    }

    @Override
    public Collection<String> getFilterNames() {
        return bloomFilterContainer.keySet();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<LocalBloomFilterConfigItem> items = config.getItems();
        if (CollectionUtils.isNotEmpty(items)) {
            items.stream().forEach(item -> {
                String funnelTypeName = item.getFunnelTypeName();
                FunnelType funnelType = FunnelType.valueOf(funnelTypeName);
                if (Objects.nonNull(funnelType)) {
                    bloomFilterContainer.putIfAbsent(item.getName(), new LocalBloomFilter(funnelType.getFunnel(),
                            item.getExpectedInsertions(), item.getFpp()));
                }
            });
        }
    }
}
