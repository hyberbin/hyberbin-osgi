package org.jplus.osgi.runner;

import org.jplus.osgi.bean.ModuleStatus;
import org.jplus.osgi.loader.ModuleLoader;
import org.jplus.osgi.util.Reflections;

/**
 * Created by hyberbin on 15/11/13.
 */
public class MainClassRunner implements ModuleRunner {

    private final String mainClass;

    public MainClassRunner(String mainClass) {
        this.mainClass = mainClass;
    }

    @Override
    public void run(ModuleLoader loader) {
        try {
            Class aClass = loader.getClass(mainClass, true);
            Reflections.invokeMethod(aClass,"main",new Class[]{String[].class},new String[]{null});
            loader.getModulesBean().setStatus(ModuleStatus.running);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
