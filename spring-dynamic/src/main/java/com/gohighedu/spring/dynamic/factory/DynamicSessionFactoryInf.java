package com.gohighedu.spring.dynamic.factory;

/**
 * Created by hyberbin on 15/11/17.
 */
import org.hibernate.SessionFactory;

public interface DynamicSessionFactoryInf extends SessionFactory {

    public SessionFactory getHibernateSessionFactory();
}