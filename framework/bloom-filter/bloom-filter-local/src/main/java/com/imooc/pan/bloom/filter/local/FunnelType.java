package com.imooc.pan.bloom.filter.local;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * 数据类型通道枚举类
 */
@AllArgsConstructor
@Getter
public enum FunnelType {

    LONG(Funnels.longFunnel()),
    INTEGER(Funnels.integerFunnel()),
    STRING(Funnels.stringFunnel(StandardCharsets.UTF_8));

    private Funnel funnel;
}
