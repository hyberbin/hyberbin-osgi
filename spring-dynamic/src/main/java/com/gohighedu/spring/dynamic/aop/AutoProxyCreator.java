package com.gohighedu.spring.dynamic.aop;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ClassUtils;


/**
 * 创建AOP代理对象的时候如果用以前的AspectJAwareAdvisorAutoProxyCreator则ClassLoader不对.
 * Created by hyberbin on 15/11/18.
 */
public class AutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

    /**
     * 重写createProxy方法,获取classLoader的时候取当前线程的ClassLoader.
     * @param beanClass
     * @param beanName
     * @param specificInterceptors
     * @param targetSource
     * @return
     */
    @Override
    protected Object createProxy(Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {
        ProxyFactory proxyFactory = new ProxyFactory();
        // Copy our properties (proxyTargetClass etc) inherited from ProxyConfig.
        proxyFactory.copyFrom(this);

        if (!shouldProxyTargetClass(beanClass, beanName)) {
            // Must allow for introductions; can't just set interfaces to
            // the target's interfaces only.
            Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, Thread.currentThread().getContextClassLoader());
            for (Class<?> targetInterface : targetInterfaces) {
                proxyFactory.addInterface(targetInterface);
            }
        }

        Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
        for (Advisor advisor : advisors) {
            proxyFactory.addAdvisor(advisor);
        }

        proxyFactory.setTargetSource(targetSource);
        customizeProxyFactory(proxyFactory);

        proxyFactory.setFrozen(isFrozen());
        if (advisorsPreFiltered()) {
            proxyFactory.setPreFiltered(true);
        }

        return proxyFactory.getProxy(Thread.currentThread().getContextClassLoader());
    }
}
