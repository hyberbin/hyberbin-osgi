/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.installer;

import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.loader.ModuleLoader;

/**
 * 模块安装器
 *
 * @author hyberbin
 */
public interface ModuleInstaller {

    ModuleLoader install(ModulesBean modulesBean);


}
