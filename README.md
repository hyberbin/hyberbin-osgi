# hyberbin-osgi
一个简单的osgi实现框架，可以动态装载运行，jar包。

## 运行方式
```java

public static void main(String[] args) throws Exception {
        //模块名称
        final String module="comparedb";
        //获取OSGI容器实例
        final OSGiContainer container = OSGiContainer.getInstance();
        //启动框架
        container.start();
        //设置模块安装成功后的处理
        container.setModuleInstalledHandler(new ModuleInstalledHandler() {
            @Override
            public void moduleInstalled(ModuleLoader loader) {
                //模块安装成功后运行该模块
                Thread.currentThread().setContextClassLoader((ClassLoader)container.getLoader(module));
                container.run(module);
            }
        });
        //创建模块模型
        ModulesBean modulesBean = new ModulesBean(module, "1.0", new JarFile[]{new JarFile("/codes/myproject/CompareDB/target/CompareDB.jar")},new MainClassRunner("com.hyberbin.main.Main"));
        //从本地maven仓库中自动解决依赖
        modulesBean.setModuleLoader(new MavenModuleLoaderImpl("/Users/hyberbin/.m2/repository/"));
        //安装
        container.install(modulesBean);
    }
    
```

## 动态安装一个SpringMVC模块

spring-dynamic文件夹下是动态安装一个SpringMVC模块的实现


### applicationContext-dao.xml配置如下:
 
 ```xml
 
 <?xml version="1.0" encoding="UTF-8"?>
 <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
             http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.2.xsd"
        default-lazy-init="true">
 
     <bean class="org.springframework.orm.hibernate4.HibernateExceptionTranslator"/>
     <!--<bean class="org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>-->
     <!--自己实现了一个PersistenceExceptionTranslationPostProcessor,原来的获取ClassLoader的方式有问题-->
     <bean class="com.XXX.spring.dynamic.aop.UserPersistenceExceptionTranslationPostProcessor"/>
     <!--让这个sessionFactory来动态路由多个sessionFactory-->
     <bean id="sessionFactory" class="com.XXX.spring.dynamic.factory.DynamicSessionFactory"/>
     <!--这个是将所有的sessionFactory放入Map中-->
     <bean id="sessionFactoryMap" class="java.util.HashMap">
         <constructor-arg>
             <map>
                 <entry key="defaultSessionFactory">
                     <ref bean="defaultSessionFactory"/>
                 </entry>
             </map>
         </constructor-arg>
     </bean>
     <!-- Hibernate SessionFactory -->
     <bean id="defaultSessionFactory" lazy-init="true" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean" destroy-method="destroy">
         <property name="dataSource" ref="dataSource"/>
         <property name="configLocation" value="/WEB-INF/hibernate.cfg.xml"/>
         <property name="packagesToScan">
             <list>
                 <value>com.XXX.digitalcampus.**.model</value>
             </list>
         </property>
         <property name="hibernateProperties">
             <props>
                 <prop key="hibernate.dialect">${hibernate.dialect}</prop>
                 <prop key="hibernate.show_sql">false</prop>
                 <prop key="hibernate.format_sql">false</prop>
                 <prop key="hibernate.query.substitutions">true 'Y', false 'N'</prop>
                 <prop key="hibernate.cache.use_second_level_cache">true</prop>
                 <prop key="hibernate.cache.provider_class">org.hibernate.cache.ehcache.EhCacheRegionFactory</prop>
                 <prop key="javax.persistence.validation.mode">none</prop>
                 <!--<prop key="hibernate.current_session_context_class">org.springframework.orm.hibernate4.SpringSessionContext</prop>-->
                 <!--<prop key="hibernate.transaction.auto_close_session">true</prop>-->
                 <!-- Hibernate Search index directory -->
                 <!--<prop key="hibernate.search.default.indexBase">${app.search.index.basedir}</prop>-->
             </props>
             <!-- Turn batching off for better error messages under PostgreSQL -->
             <!-- hibernate.jdbc.batch_size=0 -->
         </property>
     </bean>
 
     <!-- Transaction manager for a single Hibernate SessionFactory (alternative to JTA) -->
     <!--<bean id="txManager" class="org.springframework.orm.hibernate4.HibernateTransactionManager">-->
         <!--<property name="sessionFactory" ref="sessionFactory"/>-->
     <!--</bean>-->
     <!--动态事务管理器-->
     <bean id="txManager" class="com.XXX.spring.dynamic.factory.DynamicTransactionManager">
         <property name="sessionFactory" ref="sessionFactory"/>
     </bean>
     <!-- Activates scanning of @Repository -->
     <context:component-scan base-package="com.XXX.framework.**.data.hibernate"/>
     <context:component-scan base-package="com.XXX.digitalcampus.**.dao.hibernate"/>
 
 </beans>

 
 ```
 
###  applicationContext-service.xml配置如下:
  
  ```xml
 
 <?xml version="1.0" encoding="UTF-8"?>
 <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:aop="http://www.springframework.org/schema/aop"
        xmlns:context="http://www.springframework.org/schema/context"
        xmlns:tx="http://www.springframework.org/schema/tx"
        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
             http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.2.xsd
             http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.2.xsd
             http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-3.2.xsd"
        default-lazy-init="true">
 
     <!-- =================================================================== -->
     <!-- AOP: Configuration and Aspects                                      -->
     <!-- =================================================================== -->
 
     <tx:advice id="txAdvice" transaction-manager="txManager">
         <tx:attributes>
             <tx:method name="save*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="add*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="create*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="insert*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="update*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="merge*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="del*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="remove*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="put*" rollback-for="com.XXX.framework.core.exception.BusinessException"/>
             <tx:method name="execute*" rollback-for="com.XXX.framework.core.exception.BusinessException"
                        no-rollback-for="java.lang.RuntimeException"/>
             <tx:method name="clear*" rollback-for="com.XXX.framework.core.exception.BusinessException"
                        no-rollback-for="java.lang.RuntimeException"/>
             <tx:method name="disable*" rollback-for="com.XXX.framework.core.exception.BusinessException"
                        no-rollback-for="java.lang.RuntimeException"/>
             <tx:method name="use*" rollback-for="com.XXX.framework.core.exception.BusinessException"
                        no-rollback-for="java.lang.RuntimeException"/>
 
             <!--hibernate4必须配置为开启事务 否则 getCurrentSession()获取不到-->
             <tx:method name="get*" read-only="true"/>
             <tx:method name="count*" read-only="true"/>
             <tx:method name="find*" read-only="true"/>
             <tx:method name="list*" read-only="true"/>
             <tx:method name="*" read-only="false"/>
         </tx:attributes>
     </tx:advice>
     <!--这个modulesAdvice主要是为了在ThreadLocal中放入一个模块名,后面好获取对应模块的sessionFactory-->
     <bean id="modulesAdvice" class="com.XXX.spring.dynamic.aop.ServiceMethodBeforeAdvice"/>
 
 
     <!--&lt;!&ndash; 配置pointcut，将事务通过aop方式插入到joinpoint &ndash;&gt;-->
     <!--<aop:config>-->
         <!--<aop:pointcut expression="execution(* com.XXX..service.impl.*.*(..))" id="serviceMethod"/>-->
         <!--&lt;!&ndash;这个modulesAdvice主要是为了在ThreadLocal中放入一个模块名,后面好获取对应模块的sessionFactory&ndash;&gt;-->
         <!--<aop:advisor advice-ref="modulesAdvice" pointcut-ref="serviceMethod"/>-->
         <!--<aop:advisor advice-ref="txAdvice" pointcut-ref="serviceMethod"/>-->
     <!--</aop:config>-->
     <!--<aop:aspectj-autoproxy proxy-target-class="true"/>-->
     <!--这里没用<aop:>这种高级的配置因为AutoProxyCreator是我自己实现的,之前的AutoProxyCreator不能获取当前的ClassLoader-->
     <bean id="autoProxyCreator" class="com.XXX.spring.dynamic.aop.AutoProxyCreator"></bean>
     <!--第一个注入modulesAdvice-->
     <bean id="aspectjAspect" class="org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor">
         <property name="advice" ref="modulesAdvice"/>
         <property name="expression" value="execution(* com.XXX..service.impl.*.*(..))"/>
     </bean>
     <!--第二个注入modulesAdvice,注入事务代理,要从第一个切入点中获取当前模块的SessionFactory-->
     <bean id="aspectjAspect2" class="org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor">
         <property name="advice" ref="txAdvice"/>
         <property name="expression" value="execution(* com.XXX..service.impl.*.*(..))"/>
     </bean>
     <!--osgi模块的卸载器-->
     <bean id="moduleUnInstaller" class="com.XXX.spring.dynamic.uninstaller.SpringModuleUninstall"/>
     <!--启动OSGI容器-->
     <bean id="osgi-container" class="org.jplus.osgi.core.OSGiContainer" factory-method="getInstance" lazy-init="false" init-method="start">
         <property name="moduleUnInstaller" ref="moduleUnInstaller"/>
     </bean>
 
     <!-- Activates scanning of @Service -->
     <context:component-scan base-package="com.XXX.framework.**.service"/>
     <context:component-scan base-package="com.XXX.digitalcampus.**.service"/>
 
     <!-- Configure Velocity for sending e-mail -->
     <bean id="velocityEngine" class="org.springframework.ui.velocity.VelocityEngineFactoryBean">
         <property name="velocityProperties">
             <props>
                 <prop key="resource.loader">class</prop>
                 <prop key="class.resource.loader.class">
                     org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
                 </prop>
                 <prop key="velocimacro.library"></prop>
             </props>
         </property>
     </bean>
 
 </beans>
```

## 说明
作者已经实现了用这个框架动态加载自己项目中的模块.
* 动态装载Controller类,并注册BeanFactory缓存服务.
* 动态装载Service类,并注册Aop服务和BeanFactory缓存服务,开启Hibernate事务.
* 动态装载Hibernate类,并动态生成和加载新的SessionFactory,和BeanFactory缓存服务.
* 自动将jar包中的jsp文件解压到web容器下.

## 问题申明
* 这种osgi平台下的模块中如果有单独的线程或者不能正常中断的程序将导致模块只能加载不能卸载!
* 程序中不能有System.exit()方法的调用.