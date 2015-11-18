/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.core;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jplus.osgi.bean.ModuleStatus;
import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.installer.ModuleInstaller;
import org.jplus.osgi.installer.ModuleInstallerImpl;
import org.jplus.osgi.installer.ModuleUnInstaller;
import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.installer.handler.ModuleInstalledHandler;
import org.jplus.osgi.loader.handler.ModuleLoadedHandler;
import org.jplus.osgi.runner.ModuleRunner;

/**
 * 模块容器.
 * @author hyberbin
 */
public class OSGiContainer {

    //单例模式下唯一实例
    private static final OSGiContainer INSTANCE = new OSGiContainer();
    private final Map<String, ModuleLoader> modulesMap = new HashMap<String, ModuleLoader>();
    //定时器
    private final Timer timer = new Timer("OSGiContainerTimer");
    //安装队列
    private final Queue<ModulesBean> installQueue = new LinkedList<ModulesBean>();
    //卸载队列
    private final Queue<String> unInstallQueue = new LinkedList<String>();
    //安装模块的间隔
    private static final long install_period = 500;
    //卸载模块的间隔
    private static final long un_install_period = 500;

    private ModuleInstaller moduleInstaller;
    private ModuleUnInstaller moduleUnInstaller;
    private ModuleInstalledHandler moduleInstalledHandler;

    private OSGiContainer() {
        moduleInstaller = new ModuleInstallerImpl(new ModuleLoadedHandler() {
            @Override
            public void moduleLoaded(ModuleLoader loader) {
                Logger.getLogger(ModuleInstallerImpl.class.getName()).log(Level.INFO, "模块:" + loader.getModulesBean() + "加载完毕");
            }
        });
    }

    public static OSGiContainer getInstance() {
        return INSTANCE;
    }

    public void start() {
        //安装队列维护
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (!installQueue.isEmpty()) {
                    ModulesBean modulesBean = installQueue.poll();
                    ModuleLoader moduleLoader=moduleInstaller.install(modulesBean);
                    modulesMap.put(modulesBean.getName(), moduleLoader);
                    modulesBean.setInstallTime(new Date());
                    if(moduleInstalledHandler!=null){
                        moduleInstalledHandler.moduleInstalled(moduleLoader);
                    }
                }
            }
        }, new Date(), install_period);
        //卸载队列维护
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (!unInstallQueue.isEmpty()) {
                    String module = unInstallQueue.poll();
                    ModuleLoader remove = modulesMap.remove(module);
                    if (remove != null) {
                        ModulesBean modulesBean = remove.getModulesBean();
                        if (moduleUnInstaller != null) {
                            moduleUnInstaller.unInstall(modulesBean);
                        }
                        modulesBean.setStatus(ModuleStatus.removed);
                        modulesBean.setUninstallTime(new Date());
                    }
                }
            }
        }, new Date(), un_install_period);
    }

    /**
     * 安装模块
     *
     * @param modulesBean
     */
    public void install(ModulesBean modulesBean) {
        installQueue.add(modulesBean);
    }

    public void run(String module){
        final ModuleLoader moduleLoader = modulesMap.get(module);
        if(moduleLoader!=null){
            final ModulesBean modulesBean = moduleLoader.getModulesBean();
            final ModuleRunner moduleRunner = modulesBean.getModuleRunner();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    modulesBean.setStatus(ModuleStatus.starting);
                    moduleRunner.run(moduleLoader);
                }
            };
            thread.start();
        }
    }

    /**
     * 卸载模块
     *
     * @param module
     */
    public void unInstall(String module) {
        unInstallQueue.add(module);
    }

    public ModuleInstaller getModuleInstaller() {
        return moduleInstaller;
    }

    public void setModuleInstaller(ModuleInstaller moduleInstaller) {
        this.moduleInstaller = moduleInstaller;
    }

    public ModuleUnInstaller getModuleUnInstaller() {
        return moduleUnInstaller;
    }

    public void setModuleUnInstaller(ModuleUnInstaller moduleUnInstaller) {
        this.moduleUnInstaller = moduleUnInstaller;
    }

    public ModuleInstalledHandler getModuleInstalledHandler() {
        return moduleInstalledHandler;
    }

    public void setModuleInstalledHandler(ModuleInstalledHandler moduleInstalledHandler) {
        this.moduleInstalledHandler = moduleInstalledHandler;
    }

    public ModuleLoader getLoader(String module) {
        return modulesMap.get(module);
    }

    public Class getClass(String module, String className) {
        ModuleLoader loader = modulesMap.get(module);
        if (loader == null) {
            throw new IllegalArgumentException("找不到模块:" + module);
        }
        try {
            return loader.getClass(className, true);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("模块:" + module + " 中找不到类" + className);
        }
    }

}
