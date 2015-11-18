/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jplus.osgi.loader;

/**
 * @author hyberbin
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jplus.osgi.bean.ModulesBean;
import org.jplus.osgi.loader.handler.ClassLoadedHandler;
import org.jplus.osgi.loader.handler.ModuleLoadedHandler;
import org.jplus.osgi.loader.handler.ResourceLoadedHandler;

public class ModuleLoaderImpl extends ClassLoader implements ModuleLoader {

    //资源缓存
    private final Map resources = new HashMap();
    //class资源及实体缓存
    private final Map<String, byte[]> classBuffersMap = new HashMap();
    //扫描到的类
    private final List<String> classNames = new ArrayList();
    //根据类名和类的映射
    private final Map<String, Class> classMap = new HashMap<>();
    //资源和jar包的映射关系,资源索引为类名或者资源路径
    private final Map<String, String> resourceJarMap = new HashMap<String, String>();
    //类被加载后的处理器
    protected ClassLoadedHandler classLoadedHandler;
    //模块加载后的处理器
    protected ModuleLoadedHandler moduleLoadedHandler;
    //资源加载后的处理器
    protected ResourceLoadedHandler resourceLoadedHandler;
    //模块模型
    protected ModulesBean modulesBean;

    public ModuleLoaderImpl(ClassLoader parent) {
        super(parent);
    }

    public ModuleLoaderImpl() {
    }

    /**
     * 加载完Jar后定义类
     */
    protected void defineAllClass() {
        while (classNames.size() > 0) {
            //获得类路径全长
            int n = classNames.size();
            for (int i = classNames.size() - 1; i >= 0; i--) {
                String className = classNames.get(i);
                try {
                    //查询指定类
                    if (classMap.containsKey(className)) {
                        continue;
                    }
                    Class<?> defineClass = defineClass(className,
                            (byte[]) classBuffersMap.get(className), 0,
                            ((byte[]) classBuffersMap.get(className)).length);
                    //获得类名
                    String pkName = (String) classNames.get(i);
                    if (pkName.lastIndexOf('.') >= 0) {
                        pkName = pkName.substring(0, pkName.lastIndexOf('.'));
                        if (getPackage(pkName) == null) {
                            definePackage(pkName, null, null, null,null, null, null, null);
                        }
                    }
                    classMap.put(className, defineClass);
                    //查询后删除缓冲
                    classNames.remove(i);
                    if (classLoadedHandler != null) {
                        classLoadedHandler.classLoaded(defineClass);
                    }
                } catch (NoClassDefFoundError e) {
                    //由于依赖找不到加载不了等待下次再加载
                } catch (UnsupportedClassVersionError e) {
                    //jre版本错误提示
                    throw new UnsupportedClassVersionError(classNames.get(i) + ", " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")");
                } catch (java.lang.LinkageError e) {
                    //当前classLoader中已经加载过该类
                    //e.printStackTrace();
                }
            }
            if (n == classNames.size()) {
                for (String className1 : classNames) {
                    System.err.println("NoClassDefFoundError:"+resourceJarMap.get(className1)+"!" + className1);
                }
                break;
            }
        }
    }

    /**
     * 加载jar文件
     *
     * @param jar
     * @throws Exception
     */
    protected void load(JarFile jar) throws Exception {
        // 依次获得对应JAR文件中封装的各个被压缩文件的JarEntry
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryName = entry.getName();
            // 当找到的entry为class时
            InputStream inputStream = jar.getInputStream(entry);
            byte[] resourceData = getResourceData(inputStream);
            if (entryName.toLowerCase().endsWith(".class")) {
                // 将类路径转变为类全称
                String name = entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
                // 加载该类
                byte[] data = resourceData;
                classBuffersMap.put(name, data);
                classNames.add(name);
                resourceJarMap.put(name, jar.getName());

            } else {
                // 非class结尾但开头字符为'/'时
                if (entryName.charAt(0) == '/') {
                    resources.put(entryName, resourceData);
                    // 否则追加'/'后缓存    
                } else {
                    resources.put("/" + entryName, resourceData);
                }
                if(resourceLoadedHandler!=null){
                    resourceLoadedHandler.loadedResource(entryName,resourceData);
                }
            }
            inputStream.close();
            resourceJarMap.put(entryName, jar.getName());
        }
        //当获得的main-class名不为空时
    }

    @Override
    public void load(ModulesBean modulesBean) throws Exception {
        if (this.modulesBean == null) {
            for (JarFile jar : modulesBean.getJarFiles()) {
                load(jar);
            }
            defineAllClass();
            this.modulesBean = modulesBean;
            if (moduleLoadedHandler != null) {
                moduleLoadedHandler.moduleLoaded(this);
            }
        } else {
            throw new IllegalArgumentException("模块:" + modulesBean.getName() + "已经初始化");
        }
    }

    /** */
    /**
     * 获得指定JarInputStream的byte[]形式
     *
     * @param jar
     * @return
     * @throws IOException
     */
    private static byte[] getResourceData(InputStream jar) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int size;
        while (jar.available() > 0) {
            size = jar.read(buffer);
            if (size > 0) {
                data.write(buffer, 0, size);
            }
        }
        return data.toByteArray();
    }

    /**
     * 重载的getResource,检查是否重复包含
     *
     * @return
     */
    @Override
    public URL getResource(String name) {
        if (resourceJarMap.containsKey(name)) {
            try {
                return new URL("jar","","file:" + resourceJarMap.get(name) + "!/" + name);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        if(getParent()!=null){
            return getParent().getResource(name);
        }else{
            throw new IllegalArgumentException("资源:"+name+" 找不到!");
        }
    }

    @Override
    protected URL findResource(String name) {
        if (resourceJarMap.containsKey(name)) {
            try {
                return new URL("jar","","file:" + resourceJarMap.get(name) + "!/" + name);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        if(getParent()!=null){
           return getParent().getResource(name);
        }else{
            throw new IllegalArgumentException("资源:"+name+" 找不到!");
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return java.util.Collections.emptyEnumeration();
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return java.util.Collections.emptyEnumeration();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class aClass = classMap.get(name);
        return aClass == null ? super.findClass(name) : aClass;
    }

    /**
     * 重载的getResourceAsStream,检查是否重复包含
     *
     * @return
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }
        if (resources.containsKey("/" + name)) {
            return new ByteArrayInputStream((byte[]) resources.get("/" + name));
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public byte[] getClassByte(String name) {
        return classBuffersMap.get(name);
    }

    @Override
    public Class getClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, this);
    }

    public ClassLoadedHandler getClassLoadedHandler() {
        return classLoadedHandler;
    }

    @Override
    public void setClassLoadedHandler(ClassLoadedHandler classLoadedHandler) {
        this.classLoadedHandler = classLoadedHandler;
    }

    public ModuleLoadedHandler getModuleLoadedHandler() {
        return moduleLoadedHandler;
    }

    @Override
    public ResourceLoadedHandler getResourceLoadedHandler() {
        return resourceLoadedHandler;
    }

    @Override
    public void setResourceLoadedHandler(ResourceLoadedHandler resourceLoadedHandler) {
        this.resourceLoadedHandler=resourceLoadedHandler;
    }

    @Override
    public void setModuleLoadedHandler(ModuleLoadedHandler moduleLoadedHandler) {
        this.moduleLoadedHandler = moduleLoadedHandler;
    }


    @Override
    public ModulesBean getModulesBean() {
        return modulesBean;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("----加载器被销毁---");
    }
}
