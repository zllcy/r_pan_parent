package com.imooc.pan.lock.core.key;

import com.google.common.base.Joiner;
import com.imooc.pan.lock.core.LockContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.util.Lists;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 标准的key生成器
 */
public class StandardKeyGenerator extends AbstractKeyGenerator {

    /**
     * 标准key的生成格式：
     * className:methodName:parameterType1:parameterType2:...:value1:value2:...
     *
     * @param lockContext
     * @param keyValueMap
     * @return
     */
    @Override
    protected String doGenerateKey(LockContext lockContext, Map<String, String> keyValueMap) {
        List<String> keyList = Lists.newArrayList();
        keyList.add(lockContext.getClassName());
        keyList.add(lockContext.getMethodName());
        Class[] parameterTypes = lockContext.getParameterTypes();
        if (ArrayUtils.isNotEmpty(parameterTypes)) {
            Arrays.stream(parameterTypes).forEach(parameterType -> {
                keyList.add(parameterType.toString());
            });
        } else {
            keyList.add(Void.class.toString());
        }
        Collection<String> values = keyValueMap.values();
        if (CollectionUtils.isNotEmpty(values)) {
            values.stream().forEach(value -> keyList.add(value));
        }
        return Joiner.on(":").join(keyList);
    }
}
