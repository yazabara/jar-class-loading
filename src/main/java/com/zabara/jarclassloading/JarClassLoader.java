package com.zabara.jarclassloading;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Yaroslav_Zabara on 8/4/2014.
 */
public class JarClassLoader extends ClassLoader{

    Logger logger = Logger.getLogger(JarClassLoader.class);



    @Deprecated
    public static Class loadJars(String jarFilePath, String className) throws MalformedURLException, ClassNotFoundException {
        File file  = new File(jarFilePath);
        URL url = file.toURL();
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);

        return cl.loadClass(className);
    }
}
