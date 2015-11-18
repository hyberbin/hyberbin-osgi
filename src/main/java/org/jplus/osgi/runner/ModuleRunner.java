package org.jplus.osgi.runner;

import org.jplus.osgi.loader.ModuleLoader;

/**
 * 运行模块的接口
 * Created by hyberbin on 15/11/13.
 */
public interface ModuleRunner{
    void run(ModuleLoader loader);
}
