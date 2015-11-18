/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.loader.handler;

/**
 * 当一个类加载后的处理器
 *
 * @author hyberbin
 */
public interface ClassLoadedHandler {
    /***
     * 类被加载后的处理
     * @param clazz 被成功加载的类
     */
    void classLoaded(Class clazz);
}
