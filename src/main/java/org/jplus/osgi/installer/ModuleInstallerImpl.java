/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.installer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jplus.osgi.bean.ModuleStatus;
import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.loader.handler.ClassLoadedHandler;
import org.jplus.osgi.loader.handler.ModuleLoadedHandler;

/**
 *
 * @author hyberbin
 */
public class ModuleInstallerImpl implements ModuleInstaller {
    private ModuleLoadedHandler moduleLoadedHandler;

    public ModuleInstallerImpl(ModuleLoadedHandler moduleLoadedHandler) {
        this.moduleLoadedHandler = moduleLoadedHandler;
    }

    @Override
    public ModuleLoader install(ModulesBean modulesBean) {
        try {
            ModuleLoader moduleLoader = modulesBean.getModuleLoader();
            if(moduleLoader.getClassLoadedHandler()==null){
                moduleLoader.setClassLoadedHandler(new ClassLoadedHandler() {

                    @Override
                    public void classLoaded(Class clazz) {
                        Logger.getLogger(ModuleInstallerImpl.class.getName()).log(Level.INFO, "类:" + clazz.getName() + "加载完毕");
                    }
                });
            }
            modulesBean.setStatus(ModuleStatus.loading);
            moduleLoader.load(modulesBean);
            modulesBean.setStatus(ModuleStatus.loaded);
            if(moduleLoadedHandler!=null){
                moduleLoadedHandler.moduleLoaded(moduleLoader);
            }
            return moduleLoader;
        } catch (Exception ex) {
            Logger.getLogger(ModuleInstallerImpl.class.getName()).log(Level.SEVERE, "加载模块:" + modulesBean.getName() + "出错", ex);
        }
        return null;
    }



}
