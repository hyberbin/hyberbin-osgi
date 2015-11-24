/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.core.OSGiContainer;
import org.jplus.osgi.loader.MavenModuleLoaderImpl;
import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.installer.handler.ModuleInstalledHandler;
import org.jplus.osgi.runner.MainClassRunner;

import javax.swing.*;

/**
 * @author hyberbin
 */
public class Test {

    public static void main(String[] args) throws Exception {
        final String module = "comparedb";
        List<JarFile> jarFiles = new ArrayList<>();
        String path = "/黄迎斌/ProjectBuilder/CompareDB";
        //String path = "/codes/myproject/CompareDB/target/";
        File file = new File(path);
        File[] listFiles = file.listFiles();
        for (File listFile : listFiles) {
            if (listFile.getName().endsWith(".jar")) {
                jarFiles.add(new JarFile(listFile));
            }
        }
        final OSGiContainer container = OSGiContainer.getInstance();
        container.start();
        container.setModuleInstalledHandler(new ModuleInstalledHandler() {
            @Override
            public void moduleInstalled(ModuleLoader loader) {
                Thread.currentThread().setContextClassLoader((ClassLoader) container.getLoader(module));
                container.run(module);
            }
        });
        ModulesBean modulesBean = new ModulesBean(module, "1.0", jarFiles.toArray(new JarFile[]{}), new MainClassRunner("com.hyberbin.main.Main"));
        modulesBean.setModuleLoader(new MavenModuleLoaderImpl("/Users/hyberbin/.m2/repository/"));
        container.install(modulesBean);
        Thread.sleep(5000);
        container.unInstall(module);

    }
}
