package com.zabara.jarclassloading;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Yaroslav on 03.08.2014.
 */
public class JarClassLoader {

    public static Class loadJars(String jarFilePath, String className) throws MalformedURLException, ClassNotFoundException {
        File file  = new File(jarFilePath);
        URL url = file.toURL();
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);

        return cl.loadClass(className);
    }


}
