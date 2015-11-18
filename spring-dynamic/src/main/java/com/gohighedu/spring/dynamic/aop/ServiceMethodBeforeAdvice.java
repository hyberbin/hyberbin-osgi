package com.gohighedu.spring.dynamic.aop;

import org.jplus.osgi.loader.ModuleLoader;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * 在Service层方法执行前将当前的模块名放到ThreadLocal中去.
 * 只有这样动态获取事务的时候就能根据模块的不同加载不同的SessionFactory.
 * Created by hyberbin on 15/11/17.
 */
public class ServiceMethodBeforeAdvice implements org.aopalliance.aop.Advice ,MethodBeforeAdvice {
    private static final ThreadLocal<String> threadLocal=new ThreadLocal<String>();

    public String getMoudleName(){
        String name = threadLocal.get();
        return name==null?"defaultSessionFactory":name;
    }

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        ClassLoader classLoader =target.getClass().getClassLoader();
        if(classLoader instanceof ModuleLoader){
            String name = ((ModuleLoader) classLoader).getModulesBean().getName();
            threadLocal.set(name);
        }
    }
}
