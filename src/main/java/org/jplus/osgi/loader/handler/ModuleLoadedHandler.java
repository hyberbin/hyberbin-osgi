/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.loader.handler;

import org.jplus.osgi.loader.ModuleLoader;

/**
 * jar包被加载后的处理
 * @author hyberbin
 */
public interface ModuleLoadedHandler {

    /** *
     * jar包被加载
     *
     * @param loader
     */
    void moduleLoaded(ModuleLoader loader);
}
