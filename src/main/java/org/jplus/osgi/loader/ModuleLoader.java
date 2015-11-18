/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.loader;

import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.loader.handler.ClassLoadedHandler;
import org.jplus.osgi.loader.handler.ModuleLoadedHandler;
import org.jplus.osgi.loader.handler.ResourceLoadedHandler;

/**
 * @author hyberbin
 */
public interface ModuleLoader {

    /**
     * 读取jar文件中的内容
     *
     * @param modulesBean
     * @throws Exception
     */
    void load(ModulesBean modulesBean) throws Exception;

    /**
     * 获取一个类的字节内容
     *
     * @param name
     * @return
     */
    byte[] getClassByte(String name);

    /**
     * 获取一个定义好的类
     *
     * @param name
     * @param initialize
     * @return
     * @throws ClassNotFoundException
     */
    Class getClass(String name, boolean initialize) throws ClassNotFoundException;

    /**
     * 设置类加载后的处理器
     *
     * @param classLoadedHandler
     */
    void setClassLoadedHandler(ClassLoadedHandler classLoadedHandler);

    ClassLoadedHandler getClassLoadedHandler();

    /**
     * 获取模块模型
     *
     * @return
     */
    ModulesBean getModulesBean();

    void setModuleLoadedHandler(ModuleLoadedHandler moduleLoadedHandler);

    ModuleLoadedHandler getModuleLoadedHandler();

    ResourceLoadedHandler getResourceLoadedHandler();

    void setResourceLoadedHandler(ResourceLoadedHandler resourceLoadedHandler);


}
