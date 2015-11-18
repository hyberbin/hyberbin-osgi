package com.gohighedu.spring.dynamic.dynamic;

import com.gohighedu.framework.context.SpringContextUtil;
import com.gohighedu.spring.dynamic.aop.AutoProxyCreator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.util.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 动态处理Spring的对象缓存
 * <p>User: Zhang Kaitao
 * <p>Date: 14-1-3
 * <p>Version: 1.0
 */
public class DynamicDeployBeans {

    protected static final Log logger = LogFactory.getLog(DynamicDeployBeans.class);
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ClassLoader beanClassLoader;

    //RequestMappingHandlerMapping
    private static Method detectHandlerMethodsMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "detectHandlerMethods", Object.class);
    private static Method getMappingForMethodMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
    private static Method getMappingPathPatternsMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingPathPatterns", RequestMappingInfo.class);
    private static Method getPathMatcherMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getPathMatcher");
    private static Field handlerMethodsField =
            ReflectionUtils.findField(RequestMappingHandlerMapping.class, "handlerMethods", Map.class);
    private static Field urlMapField =
            ReflectionUtils.findField(RequestMappingHandlerMapping.class, "urlMap", MultiValueMap.class);

    private static Field injectionMetadataCacheField =ReflectionUtils.findField(AutowiredAnnotationBeanPostProcessor.class, "injectionMetadataCache");


    private static Field beanPostProcessors =ReflectionUtils.findField(DefaultListableBeanFactory.class, "beanPostProcessors");

    static {
        detectHandlerMethodsMethod.setAccessible(true);
        getMappingForMethodMethod.setAccessible(true);
        getMappingPathPatternsMethod.setAccessible(true);
        getPathMatcherMethod.setAccessible(true);
        handlerMethodsField.setAccessible(true);
        urlMapField.setAccessible(true);

        injectionMetadataCacheField.setAccessible(true);
//        beanPostProcessors.setAccessible(true);
//        List field = (List) ReflectionUtils.getField(beanPostProcessors, SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory());
//        Object dist=null;
//        for(Object o:field){
//            if(o instanceof AspectJAwareAdvisorAutoProxyCreator){
//                dist=o;
//            }
//        }
//        field.remove(dist);
//        field.add(new AutoProxyCreator());
    }

    private ApplicationContext ctx;
    private DefaultListableBeanFactory beanFactory;

    public DynamicDeployBeans(ClassLoader beanClassLoader,RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping=requestMappingHandlerMapping;
        this.beanClassLoader=beanClassLoader;
    }


    @Autowired
    public void setApplicationContext(ApplicationContext ctx) {
        if (!DefaultListableBeanFactory.class.isAssignableFrom(ctx.getAutowireCapableBeanFactory().getClass())) {
            throw new IllegalArgumentException("BeanFactory must be DefaultListableBeanFactory type");
        }
        this.ctx = ctx;
        beanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
        beanFactory.setBeanClassLoader(beanClassLoader);
        beanFactory.setTempClassLoader(beanClassLoader);
    }

    public String registerBean(Class<?> beanClass) {
        return registerBean(null, beanClass);
    }

    public String registerBean(String beanName, Class<?> beanClass) {
        Assert.notNull(beanClass, "register bean class must not null");
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(beanClass);
//        bd.setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_ALL);
//        bd.setAutowireCandidate(true);
//        bd.setLazyInit(false);//不用懒加载,直接实例化
//        bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        if (StringUtils.hasText(beanName)) {
            beanFactory.registerBeanDefinition(beanName, bd);
        } else {
            beanName=BeanDefinitionReaderUtils.registerWithGeneratedName(bd, beanFactory);
        }
        return beanName;
    }



    public void registerController(Class<?> controllerClass) {
        Assert.notNull(controllerClass, "register controller bean class must not null");
        if (!WebApplicationContext.class.isAssignableFrom(ctx.getClass())) {
            throw new IllegalArgumentException("applicationContext must be WebApplicationContext type");
        }
        String controllerBeanName = controllerClass.getName();
        removeOldControllerMapping(controllerBeanName);
        registerBean(controllerBeanName, controllerClass);
        addControllerMapping(controllerBeanName);
    }



    public void removeOldControllerMapping(String controllerBeanName) {
        if (!beanFactory.containsBean(controllerBeanName)) {
            return;
        }
        RequestMappingHandlerMapping requestMappingHandlerMapping = requestMappingHandlerMapping();
        //remove old
        Class<?> handlerType = ctx.getType(controllerBeanName);
        final Class<?> userType = ClassUtils.getUserClass(handlerType);

        Map handlerMethods = (Map) ReflectionUtils.getField(handlerMethodsField, requestMappingHandlerMapping);
        MultiValueMap urlMapping = (MultiValueMap) ReflectionUtils.getField(urlMapField, requestMappingHandlerMapping);

        final RequestMappingHandlerMapping innerRequestMappingHandlerMapping = requestMappingHandlerMapping;
        Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                return ReflectionUtils.invokeMethod(
                        getMappingForMethodMethod,
                        innerRequestMappingHandlerMapping,
                        method, userType) != null;
            }
        });

        for (Method method : methods) {
            RequestMappingInfo mapping =
                    (RequestMappingInfo) ReflectionUtils.invokeMethod(getMappingForMethodMethod, requestMappingHandlerMapping, method, userType);

            handlerMethods.remove(mapping);

            Set<String> patterns = (Set<String>) ReflectionUtils.invokeMethod(getMappingPathPatternsMethod, requestMappingHandlerMapping, mapping);

            PathMatcher pathMatcher = (PathMatcher) ReflectionUtils.invokeMethod(getPathMatcherMethod, requestMappingHandlerMapping);

            for (String pattern : patterns) {
                if (!pathMatcher.isPattern(pattern)) {
                    urlMapping.remove(pattern);
                }
            }
        }
        removeInjectCache(controllerBeanName);
    }


    private void addControllerMapping(String controllerBeanName) {

        removeOldControllerMapping(controllerBeanName);

        RequestMappingHandlerMapping requestMappingHandlerMapping = requestMappingHandlerMapping();
        //spring 3.1 开始
        ReflectionUtils.invokeMethod(detectHandlerMethodsMethod, requestMappingHandlerMapping, controllerBeanName);
    }


    private RequestMappingHandlerMapping requestMappingHandlerMapping() {
        try {
            return requestMappingHandlerMapping;
        } catch (Exception e) {
            throw new IllegalArgumentException("applicationContext must has RequestMappingHandlerMapping");
        }
    }


    public void removeInjectCache(String name) {

        AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = ctx.getBean(AutowiredAnnotationBeanPostProcessor.class);

        Map<String, InjectionMetadata> injectionMetadataMap =  (Map<String, InjectionMetadata>) ReflectionUtils.getField(injectionMetadataCacheField, autowiredAnnotationBeanPostProcessor);

        injectionMetadataMap.remove(name);
    }

}
