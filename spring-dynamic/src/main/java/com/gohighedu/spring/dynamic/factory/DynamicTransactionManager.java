package com.gohighedu.spring.dynamic.factory;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.SessionFactoryUtils;

import javax.sql.DataSource;

/**
 * Created by hyberbin on 15/11/17.
 */
public class DynamicTransactionManager extends HibernateTransactionManager {

    private static final long serialVersionUID = 1047039346475978451L;
    //重写getDataSource方法，实现动态获取
    public DataSource getDataSource() {
        DataSource sfds = SessionFactoryUtils.getDataSource(getSessionFactory());
        return sfds;
    }
    //重写getSessionFactory方法，实现动态获取SessionFactory
    public SessionFactory getSessionFactory() {
        DynamicSessionFactoryInf dynamicSessionFactory = (DynamicSessionFactoryInf) super.getSessionFactory();
        SessionFactory hibernateSessionFactory = dynamicSessionFactory.getHibernateSessionFactory();
        return hibernateSessionFactory;
    }
    //重写afterPropertiesSet，跳过数据源的初始化等操作
    public void afterPropertiesSet() {
        return;
    }

}