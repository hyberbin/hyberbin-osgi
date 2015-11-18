package org.jplus.osgi;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Created by hyberbin on 15/11/12.
 */
public class Test2 {
    public static void main(String[] args) throws Exception {
//        BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated;
//        org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
//        URL resource = Test.class.getClassLoader().getResource("org/jb2011/lnf/beautyeye/ch4_scroll/imgs/np/scroll_pane_bg1.9.png");
//        System.out.println(resource.getPath());
//        System.out.println(resource.getHost());
//        System.out.println(resource.getProtocol());
//        ///
//        //file:/Users/hyberbin/.m2/repository/org/beautyeye/beautyeye/1.0/beautyeye-1.0.jar!/org/jb2011/lnf/beautyeye/ch4_scroll/imgs/np/scroll_pane_bg1.9.png
//        URL file = new URL("jar","","file:/黄迎斌/ProjectBuilder/CompareDB/lib/beautyeye-1.0.jar!/org/jb2011/lnf/beautyeye/ch4_scroll/imgs/np/scroll_pane_bg1.9.png");
//        System.out.println(file.getPath());
//        file.openStream();
//        System.out.println("---");
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Thread> threads = allStackTraces.keySet();
        for(Thread thread:threads){
            System.out.println(thread.toString());
        }
    }
}
