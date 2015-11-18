package com.gohighedu.spring.dynamic.aop;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;

/**
 * 重写了postProcessAfterInitialization让不同的ClassLoader加载的类也能动态代理
 * Created by hyberbin on 15/11/16.
 */
public class UserPersistenceExceptionTranslationPostProcessor extends PersistenceExceptionTranslationPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof AopInfrastructureBean) {
            // Ignore AOP infrastructure such as scoped proxies.
            return bean;
        }
        if (isEligible(bean, beanName)) {
            if (bean instanceof Advised) {
                Advised advised = (Advised) bean;
                if (this.beforeExistingAdvisors) {
                    advised.addAdvisor(0, this.advisor);
                }
                else {
                    advised.addAdvisor(this.advisor);
                }
                return bean;
            }
            else {
                ProxyFactory proxyFactory = new ProxyFactory(bean);
                // Copy our properties (proxyTargetClass etc) inherited from ProxyConfig.
                proxyFactory.copyFrom(this);
                proxyFactory.addAdvisor(this.advisor);
                //只改了这里获取ClassLoader的方式,之前的获取方式不对
                return proxyFactory.getProxy(Thread.currentThread().getContextClassLoader());
            }
        }
        else {
            // No async proxy needed.
            return bean;
        }
    }
}
