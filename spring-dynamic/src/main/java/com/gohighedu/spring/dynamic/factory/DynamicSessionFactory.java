package com.gohighedu.spring.dynamic.factory;


import com.gohighedu.spring.dynamic.aop.ServiceMethodBeforeAdvice;
import org.hibernate.*;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import javax.naming.*;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

/**
 * ServiceMethodBeforeAdvice中从ThreadLocal中获取模块名,再根据模块名获取SessionFactory.
 * Created by hyberbin on 15/11/17.
 */
public class DynamicSessionFactory implements DynamicSessionFactoryInf ,ApplicationContextAware {

    private static final long serialVersionUID = 1L;
    private static  final ThreadLocal<String> threadLocal=new ThreadLocal<String>();

    private ApplicationContext applicationContext;


    //动态调用SessionFactory
    private SessionFactory getHibernateSessionFactory(String name) {
        Map map= (Map) applicationContext.getBean("sessionFactoryMap");
        return (SessionFactory) map.get(name);
    }
    /**
     * 实现DynamicSessionFactoryInf 接口的方法
     * ServiceMethodBeforeAdvice中从ThreadLocal中获取模块名,再根据模块名获取SessionFactory
     * @return
     */
    public SessionFactory getHibernateSessionFactory() {
        ServiceMethodBeforeAdvice bean = (ServiceMethodBeforeAdvice) applicationContext.getBean("modulesAdvice");
        return getHibernateSessionFactory(bean.getMoudleName());
    }

    //以下是实现SessionFactory接口的方法，并对当前的SessionFactory实体进行代理
    public Reference getReference() throws NamingException {
        return getHibernateSessionFactory().getReference();
    }


    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return getHibernateSessionFactory().getSessionFactoryOptions();
    }

    @Override
    public SessionBuilder withOptions() {
        return getHibernateSessionFactory().withOptions();
    }

    public Session openSession() throws HibernateException {
        return getHibernateSessionFactory().openSession();
    }


    public Session getCurrentSession() throws HibernateException {
        return getHibernateSessionFactory().getCurrentSession();
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return getHibernateSessionFactory().withStatelessOptions();
    }


    public StatelessSession openStatelessSession() {
        return getHibernateSessionFactory().openStatelessSession();
    }

    public StatelessSession openStatelessSession(Connection connection) {
        return getHibernateSessionFactory().openStatelessSession(connection);
    }

    public ClassMetadata getClassMetadata(Class entityClass) {
        return getHibernateSessionFactory().getClassMetadata(entityClass);
    }

    public ClassMetadata getClassMetadata(String entityName) {
        return getHibernateSessionFactory().getClassMetadata(entityName);
    }

    public CollectionMetadata getCollectionMetadata(String roleName) {
        return getHibernateSessionFactory().getCollectionMetadata(roleName);
    }

    public Map getAllClassMetadata() {
        return getHibernateSessionFactory().getAllClassMetadata();
    }

    public Map getAllCollectionMetadata() {
        return getHibernateSessionFactory().getAllCollectionMetadata();
    }

    public Statistics getStatistics() {
        return getHibernateSessionFactory().getStatistics();
    }

    public void close() throws HibernateException {
        getHibernateSessionFactory().close();
    }

    public boolean isClosed() {
        return getHibernateSessionFactory().isClosed();
    }

    public Cache getCache() {
        return getHibernateSessionFactory().getCache();
    }

    public void evict(Class persistentClass) throws HibernateException {
        getHibernateSessionFactory().evict(persistentClass);
    }

    public void evict(Class persistentClass, Serializable id)
            throws HibernateException {
        getHibernateSessionFactory().evict(persistentClass, id);
    }

    public void evictEntity(String entityName) throws HibernateException {
        getHibernateSessionFactory().evictEntity(entityName);
    }

    public void evictEntity(String entityName, Serializable id)
            throws HibernateException {
        getHibernateSessionFactory().evictEntity(entityName, id);
    }

    public void evictCollection(String roleName) throws HibernateException {
        getHibernateSessionFactory().evictCollection(roleName);
    }

    public void evictCollection(String roleName, Serializable id)
            throws HibernateException {
        getHibernateSessionFactory().evictCollection(roleName, id);
    }

    public void evictQueries(String cacheRegion) throws HibernateException {
        getHibernateSessionFactory().evictQueries(cacheRegion);
    }

    public void evictQueries() throws HibernateException {
        getHibernateSessionFactory().evictQueries();
    }

    public Set getDefinedFilterNames() {
        return getHibernateSessionFactory().getDefinedFilterNames();
    }

    public FilterDefinition getFilterDefinition(String filterName)
            throws HibernateException {
        return getHibernateSessionFactory().getFilterDefinition(filterName);
    }

    public boolean containsFetchProfileDefinition(String name) {
        return getHibernateSessionFactory().containsFetchProfileDefinition(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return getHibernateSessionFactory().getTypeHelper();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }



}