package com.gohighedu.spring.dynamic.uninstaller;

import com.gohighedu.framework.context.SpringContextUtil;
import com.gohighedu.spring.dynamic.dynamic.DynamicDeployBeans;
import com.gohighedu.spring.dynamic.loader.SpringModuleLoaderImpl;
import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.installer.ModuleUnInstaller;

import java.util.Map;
import java.util.Set;

/**
 * Created by hyberbin on 15/11/18.
 */
public class SpringModuleUninstall implements ModuleUnInstaller {
    @Override
    public void unInstall(ModulesBean modulesBean) {
        String name = modulesBean.getName();
        SpringModuleLoaderImpl moduleLoader = (SpringModuleLoaderImpl)modulesBean.getModuleLoader();
        Set<String> controllerClassNames = moduleLoader.getControllerClassNames();
        Set<String> serviceClassNames = moduleLoader.getServiceClassNames();
        Set<String> hibernateClassNames = moduleLoader.getHibernateClassNames();
        DynamicDeployBeans dynamicDeployBeans = moduleLoader.getDynamicDeployBeans();
        for(String controller:controllerClassNames){
            dynamicDeployBeans.removeOldControllerMapping(controller);
        }
        for(String service:serviceClassNames){
            dynamicDeployBeans.removeInjectCache(service);
        }
        for(String hibernate:hibernateClassNames){
            dynamicDeployBeans.removeInjectCache(hibernate);
        }
        Map sessionFactoryMap= (Map) SpringContextUtil.getBean("sessionFactoryMap");
        sessionFactoryMap.remove(name);
    }
}
