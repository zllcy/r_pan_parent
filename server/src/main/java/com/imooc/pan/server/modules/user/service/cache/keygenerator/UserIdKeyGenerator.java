package com.imooc.pan.server.modules.user.service.cache.keygenerator;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

@Component(value = "userIdKeyGenerator")
public class UserIdKeyGenerator implements KeyGenerator {

    private static final String USER_ID_PREFIX = "USER:ID:";

    @Override
    public Object generate(Object o, Method method, Object... objects) {
        StringBuilder stringBuilder = new StringBuilder(USER_ID_PREFIX);
        if (objects == null || objects.length == 0) {
            return stringBuilder.toString();
        }
        Serializable id;
        for (Object param : objects) {
            if (param instanceof Serializable) {
                id = (Serializable) param;
                stringBuilder.append(id);
                return stringBuilder.toString();
            }
        }
        stringBuilder.append(StringUtils.arrayToCommaDelimitedString(objects));
        return stringBuilder.toString();
    }
}
