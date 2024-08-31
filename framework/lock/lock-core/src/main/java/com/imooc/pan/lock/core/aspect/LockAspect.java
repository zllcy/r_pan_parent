package com.imooc.pan.lock.core.aspect;

import com.imooc.pan.core.exception.RPanBusinessException;
import com.imooc.pan.lock.core.LockContext;
import com.imooc.pan.lock.core.key.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * 框架分布式锁统一切面增强逻辑实现
 */
@Component
@Aspect
@Slf4j
public class LockAspect implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private LockRegistry lockRegistry;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Pointcut(value = "@annotation(com.imooc.pan.lock.core.annotation.Lock)")
    public void lockPointcut() {

    }

    @Around("lockPointcut()")
    public Object aroundLock(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;

        LockContext lockContext = LockContext.init(proceedingJoinPoint);
        Lock lock = checkAndGetLock(lockContext);

        if (Objects.isNull(lock)) {
            log.error("lock aspect get lock fail...");
            throw new RPanBusinessException("around get lock fail");
        }

        boolean lockResult = false;
        try {
            lockResult = lock.tryLock(lockContext.getAnnotation().expireSecond(), TimeUnit.SECONDS);
            if (lockResult) {
                Object[] args = proceedingJoinPoint.getArgs();
                result = proceedingJoinPoint.proceed(args);
            }
        } catch (Throwable e) {
            log.error("lock aspect tryLock exception...", e);
            throw new RPanBusinessException("around tryLock fail...");
        } finally {
            if (lockResult) {
                lock.unlock();
            }
        }
        return result;
    }

    private Lock checkAndGetLock(LockContext lockContext) {
        if (Objects.isNull(lockRegistry)) {
            log.error("the lockRegistry is not found...");
            return null;
        }
        String lockKey = getLockKey(lockContext);
        if (StringUtils.isBlank(lockKey)) {
            return null;
        }
        return lockRegistry.obtain(lockKey);
    }

    private String getLockKey(LockContext lockContext) {
        KeyGenerator keyGenerator = applicationContext.getBean(lockContext.getAnnotation().keyGenerator());
        if (Objects.isNull(keyGenerator)) {
            log.error("the keyGenerator is not found...");
            return StringUtils.EMPTY;
        }
        return keyGenerator.generateKey(lockContext);
    }

}
