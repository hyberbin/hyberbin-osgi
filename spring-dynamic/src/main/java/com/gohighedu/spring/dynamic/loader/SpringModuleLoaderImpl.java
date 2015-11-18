package com.gohighedu.spring.dynamic.loader;

import com.gohighedu.framework.context.SpringContextUtil;
import com.gohighedu.framework.service.GenericManager;
import com.gohighedu.spring.dynamic.dynamic.DynamicDeployBeans;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.loader.ModuleLoaderImpl;
import org.jplus.osgi.loader.handler.ClassLoadedHandler;
import org.jplus.osgi.loader.handler.ModuleLoadedHandler;
import org.jplus.osgi.loader.handler.ResourceLoadedHandler;
import org.jplus.osgi.util.Reflections;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.persistence.Table;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 一个包含SpringMVC风格的模块装载器.
 * Created by hyberbin on 15/11/14.
 */
public class SpringModuleLoaderImpl extends ModuleLoaderImpl {
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    private final DynamicDeployBeans dynamicDeployBeans;
    private final Set<String> hibernateClassNames=new HashSet<String>();
    private final Set<Class> modelClasses =new HashSet<Class>();
    private final Set<String> serviceClassNames=new HashSet<String>();
    private final Set<String> controllerClassNames=new HashSet<String>();
    /**
     * 成功加载一个类后的处理.
     * 根据类的类型不一样做不同的处理.
     */
    private final ClassLoadedHandler classLoadedHandler=new ClassLoadedHandler() {

        @Override
        public void classLoaded(Class clazz) {
            if(clazz.isAnnotationPresent(Controller.class)){//加载Controller文件
                dynamicDeployBeans.registerController(clazz);
                controllerClassNames.add(clazz.getName());
            }else if(clazz.isAnnotationPresent(Repository.class)){//加载Hibernate文件
                Repository annotation = (Repository)clazz.getAnnotation(Repository.class);
                String registerBean = dynamicDeployBeans.registerBean(annotation.value(),clazz);
                hibernateClassNames.add(registerBean);
            }else if(clazz.isAnnotationPresent(Service.class)){//加载service文件
                Service annotation = (Service)clazz.getAnnotation(Service.class);
                String registerBean = dynamicDeployBeans.registerBean(annotation.value(),clazz);
                serviceClassNames.add(registerBean);
            }else if(clazz.isAnnotationPresent(Table.class)){//加载model文件
                modelClasses.add(clazz);
            }
        }
    };
    /**
     * 模块加载成功后的处理.
     * 在这里将所有的model解析装载到SessionFactory.
     */
    private final ModuleLoadedHandler moduleLoadedHandler=new ModuleLoadedHandler() {
        @Override
        public void moduleLoaded(ModuleLoader loader) {
            SessionFactory sessionFactory = obtainSessionFactory(modelClasses);
            for (String hibernate:hibernateClassNames){
                Object genericDaoHibernate =SpringContextUtil.getBean(hibernate);
                dynamicHibernate(genericDaoHibernate,sessionFactory);
            }
            //SpringContextUtil.getBean让这些Bean都缓存起来,不然实际运行的时候会因为ClassLoader不一样而找不到
            for(String service:serviceClassNames){
                GenericManager genericManager =(GenericManager) SpringContextUtil.getBean(service);
            }
            for(String service:controllerClassNames){
                Object bean = SpringContextUtil.getBean(service);
            }
            log.info("动态加载模块:{}完毕",loader.getModulesBean().getName());
        }
    };
    /**
     * 资源加载成功后的处理.
     * 这里主要是将jar包中的JSP解压出来放到容器中
     */
    private final ResourceLoadedHandler resourceLoadedHandler=new ResourceLoadedHandler() {
        @Override
        public void loadedResource(String name, byte[] bytes) {
            //将jsp页面解压出来
            if(name.startsWith("pages/")&&!name.endsWith("/")){
                try {
                    File dir = new File("/codes/osgi/development/target/development-1.0-SNAPSHOT/WEB-INF/" + name.substring(0,name.lastIndexOf("/")));
                    dir.mkdirs();
                    File file = new File("/codes/osgi/development/target/development-1.0-SNAPSHOT/WEB-INF/" + name);
                    FileCopyUtils.copy(bytes,file);
                    log.info("替换了jsp文件:{}",file.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 构造方法.
     * @param requestMappingHandlerMapping 这个只能在Controller中用@Autowired获取.
     * @param parent 本加载器的父加载器
     */
    public SpringModuleLoaderImpl(RequestMappingHandlerMapping requestMappingHandlerMapping,ClassLoader parent) {
        super(parent);
        setClassLoadedHandler(classLoadedHandler);
        dynamicDeployBeans=new DynamicDeployBeans(this,requestMappingHandlerMapping);
        dynamicDeployBeans.setApplicationContext(SpringContextUtil.getApplicationContext());
        setResourceLoadedHandler(resourceLoadedHandler);
        setModuleLoadedHandler(moduleLoadedHandler);
    }

    /**
     * 为Hibernate实例注射SessionFactory.
     * @param hibernate
     * @param sessionFactory
     */
    private void dynamicHibernate(Object hibernate,SessionFactory sessionFactory){
        Reflections.invokeMethod(hibernate,"setSessionFactory",new Class[]{SessionFactory.class},new Object[]{sessionFactory});
        log.info("为hibernate类:{}注射了sessionFactory",hibernate.getClass().getName());
    }

    /**
     * 生成一个新的SessionFactory,将模块的的model解析.
     * 这里将来可以做成从资源配置文件中动态解析生成.
     * @param entityClasses
     * @return
     */
    private SessionFactory obtainSessionFactory(Collection<Class> entityClasses){
        LocalSessionFactoryBean localSessionFactoryBean =(LocalSessionFactoryBean) SpringContextUtil.getBean("&defaultSessionFactory");
        Configuration configuration = localSessionFactoryBean.getConfiguration();
        synchronized(configuration){//避免并发操作导致configuration重复添加相同的entityClass
            for(Class entityClass:entityClasses){
                if(configuration.getClassMapping(entityClass.getName())==null){
                    configuration.addAnnotatedClass(entityClass);
                }
            }
        }
        Thread.currentThread().setContextClassLoader(this);//设置当前的Classloader不然会报找不到类的错误
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        SessionFactory newSessionFactory = configuration.buildSessionFactory(serviceRegistry);
        log.info("为模块中{}个类重新生成一个新的SessionFactory",entityClasses.size());
        Map<String,SessionFactory> sessionFactoryMap =(Map<String,SessionFactory>) SpringContextUtil.getBean("sessionFactoryMap");
        sessionFactoryMap.put(getModulesBean().getName(),newSessionFactory);
        return newSessionFactory;
    }

    public Set<String> getHibernateClassNames() {
        return hibernateClassNames;
    }

    public Set<Class> getModelClasses() {
        return modelClasses;
    }

    public Set<String> getServiceClassNames() {
        return serviceClassNames;
    }

    public Set<String> getControllerClassNames() {
        return controllerClassNames;
    }

    public DynamicDeployBeans getDynamicDeployBeans() {
        return dynamicDeployBeans;
    }
}
