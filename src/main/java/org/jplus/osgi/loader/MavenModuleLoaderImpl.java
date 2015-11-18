package org.jplus.osgi.loader;

import org.jplus.osgi.bean.ModulesBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 从本地Maven仓库中自动寻找依赖并加载.
 * jar包中只用传入一个文件,根据其pom.xml自动加载依赖.
 * Created by hyberbin on 15/11/13.
 */
public class MavenModuleLoaderImpl extends ModuleLoaderImpl implements ModuleLoader {
    /**maven仓库的路径*/
    private final String repository;
    private final List<JarFile> jarFiles = new ArrayList<>();
    private final Set<String> jarNames = new HashSet<>();
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public MavenModuleLoaderImpl(String repository, ClassLoader parent) {
        super(parent);
        this.repository = repository.endsWith("/") ? repository : repository + "/";
    }

    public MavenModuleLoaderImpl(String repository) {
        this.repository = repository.endsWith("/") ? repository : repository + "/";
    }

    @Override
    public void load(ModulesBean modulesBean) throws Exception {
        if (super.modulesBean == null) {
            for (JarFile jar : modulesBean.getJarFiles()) {
                loadJarFormMaven(jar);
            }
            for (JarFile jar : jarFiles) {
                modulesBean.addJarFile(jar);
                super.load(jar);
            }
            defineAllClass();
            super.modulesBean = modulesBean;
            if (moduleLoadedHandler != null) {
                moduleLoadedHandler.moduleLoaded(this);
            }
        } else {
            throw new IllegalArgumentException("模块:" + modulesBean.getName() + "已经初始化");
        }
    }

    private void loadJarFormMaven(JarFile jar) throws Exception {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith("META-INF/maven/") && name.endsWith("pom.xml")) {//只读取pom文件
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputStream inputStream = jar.getInputStream(jarEntry);
                Document doc = builder.parse(inputStream);
                NodeList project = doc.getElementsByTagName("project");
                NodeList childNodes = project.item(0).getChildNodes();
                PomBean pomBean = getPomBean(childNodes);
                loadPomBeanJar(pomBean);
                inputStream.close();
                break;
            }
        }
    }

    /**
     * 递归解析节点到PomBean
     * @param childNodes
     * @return
     */
    private PomBean getPomBean(NodeList childNodes) {
        PomBean pomBean = new PomBean();
        Node parentNode=null;
        for(int i=0;i<childNodes.getLength();i++){
            Node item = childNodes.item(i);
            if("artifactId".equals(item.getNodeName())){
                pomBean.setArtifactId(item.getTextContent().trim());
            }else if("dependencies".equals(item.getNodeName())){
                NodeList dependencies = item.getChildNodes();
                for(int j=0;j<dependencies.getLength();j++){
                    pomBean.addDependency(getPomBean(dependencies.item(j).getChildNodes()));
                }
            }else if("groupId".equals(item.getNodeName())){
                pomBean.setGroupId(item.getTextContent().trim());
            }else if("version".equals(item.getNodeName())){
                pomBean.setVersion(item.getTextContent().trim());
            }else if("parent".equals(item.getNodeName())){
                parentNode=item;
            }
        }
        if(parentNode!=null){
            if(pomBean.getGroupId()==null){
                NodeList nodeList = parentNode.getChildNodes();
                for(int i=0;i<nodeList.getLength();i++){
                    Node item = childNodes.item(i);
                    if("groupId".equals(item.getNodeName())){
                        pomBean.setGroupId(item.getTextContent().trim());
                        break;
                    }
                }
            }
            if(pomBean.getVersion()==null){
                NodeList nodeList = parentNode.getChildNodes();
                for(int i=0;i<nodeList.getLength();i++){
                    Node item = childNodes.item(i);
                    if("version".equals(item.getNodeName())){
                        pomBean.setGroupId(item.getTextContent().trim());
                        break;
                    }
                }
            }
        }
        return pomBean;
    }

    /**
     * 递归加载所有依赖的JarFile
     * @param pomBean
     * @throws IOException
     */
    private void loadPomBeanJar(PomBean pomBean) throws IOException {
        if(!jarNames.contains(pomBean.getJarName())){
            jarNames.add(pomBean.getJarName());
            jarFiles.add(new JarFile(pomBean.getPath()));
            for(PomBean dependency:pomBean.getDependencies()){
                loadPomBeanJar(dependency);
            }
        }
    }


    class PomBean {
        private String groupId;
        private String artifactId;
        private String version;
        private Set<PomBean> dependencies=new HashSet<PomBean>();

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Set<PomBean> getDependencies() {
            return dependencies;
        }

        public void addDependency(PomBean pomBean) {
            if(pomBean.getArtifactId()!=null){
                this.dependencies.add(pomBean);
            }
        }

        /**
         * 获取jar文件名
         * @return
         */
        public String getJarName(){
            return artifactId+"-"+version+".jar";
        }

        /**
         * 获取jar文件的路径
         * @return
         */
        public String getPath(){
            return repository+groupId.replace(".","/")+"/"+artifactId+"/"+version+"/"+getJarName();
        }
    }
}
