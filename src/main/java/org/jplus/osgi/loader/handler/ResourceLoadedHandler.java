package org.jplus.osgi.loader.handler;

/**
 * 资源被加载后的处理
 * Created by hyberbin on 15/11/14.
 */
public interface ResourceLoadedHandler {

    void loadedResource(String name,byte[] bytes);
}
