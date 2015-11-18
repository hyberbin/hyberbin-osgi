/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.bean;

import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.runner.ModuleRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 模块模型.
 * 模型应该定义模块包含的jar包,模块名,版本号,装载器
 * @author hyberbin
 */
public class ModulesBean {
    /**模块名称*/
    private final String name;
    /**模块的版本*/
    private final String version;
    /**模块包含的Jar文件*/
    private final List<JarFile> jarFiles=new ArrayList<JarFile>();
    /**模块的运行器*/
    private final ModuleRunner moduleRunner;
    /**模块的装载器*/
    private  ModuleLoader moduleLoader;
    /**模块的状态*/
    private ModuleStatus status=ModuleStatus.waiting;
    /**模块的装载时间*/
    private Date installTime;
    /**模块的卸载时间*/
    private Date uninstallTime;

    public ModulesBean(String name, String version, JarFile[] jarFiles,ModuleRunner moduleRunner) {
        this.name = name;
        this.version = version;
        this.jarFiles.addAll(Arrays.asList(jarFiles));
        this.moduleRunner = moduleRunner;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<JarFile> getJarFiles() {
        return jarFiles;
    }

    public void addJarFile(JarFile jarFile) {
        jarFiles.add(jarFile);
    }

    public ModuleStatus getStatus() {
        return status;
    }

    public void setStatus(ModuleStatus status) {
        this.status = status;
    }

    public Date getInstallTime() {
        return installTime;
    }

    public void setInstallTime(Date installTime) {
        this.installTime = installTime;
    }

    public Date getUninstallTime() {
        return uninstallTime;
    }

    public void setUninstallTime(Date uninstallTime) {
        this.uninstallTime = uninstallTime;
    }

    public ModuleRunner getModuleRunner() {
        return moduleRunner;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public void setModuleLoader(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
    }
}
