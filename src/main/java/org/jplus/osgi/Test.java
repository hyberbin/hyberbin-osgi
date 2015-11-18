/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.core.OSGiContainer;
import org.jplus.osgi.loader.MavenModuleLoaderImpl;
import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.installer.handler.ModuleInstalledHandler;
import org.jplus.osgi.runner.MainClassRunner;

/**
 *
 * @author hyberbin
 */
public class Test {

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
}
