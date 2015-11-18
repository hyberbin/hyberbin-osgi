package org.jplus.osgi.installer.handler;

import org.jplus.osgi.loader.ModuleLoader;

/**
 * Created by hyberbin on 15/11/13.
 */
public interface ModuleInstalledHandler {
    /** *
     * 模块安装成功
     *
     * @param loader
     */
    void moduleInstalled(ModuleLoader loader);
}
