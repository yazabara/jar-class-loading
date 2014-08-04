package com.zabara.jarclassloading;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Created by Yaroslav_Zabara on 8/4/2014.
 */
public class JarClassLoader extends ClassLoader {

    private final static Logger logger = Logger.getLogger(JarClassLoader.class);

    private String jarPath = "";

    public JarClassLoader(ClassLoader parent, String jarPath) {
        super(parent);
        this.jarPath = jarPath;
    }

    public JarClassLoader(String jarPath) {
        super();
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        synchronized (getClassLoadingLock(name)) {
            clazz = findLoadedClass(name);
            //не загружен
            if (clazz == null) {
                try {
                    if (getParent() != null) {
                        clazz = getParent().loadClass(name);
                    }
                } catch (ClassNotFoundException ex) {
                    logger.warn("JarClassLoader parent can't find class " + name);
                }
                //если parent не нашел класс - ищем сами
                if (clazz == null) {
                    clazz = findClass(jarPath, name, "ElenaF.jar");
                }
            }
        }
        return clazz;//super.loadClass(name, resolve);
    }

    public Class<?> findClass(String jarFilePath, String className, String jarName) {
        Class result = null;
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            //нашли нашу jar в jar-файле
            JarEntry jarEntry = JarClassLoader.findJarEntry(jarFile, jarName);
            if (jarEntry == null) {
                return null;
            }

            JarInputStream jarInputStream = new JarInputStream(jarFile.getInputStream(jarEntry));

//            File jarDir = new File(jarFilePath).getParentFile();
//            File file = File.createTempFile("tmp", "", jarDir);
//            file.getParentFile().mkdir();
//            file.mkdir();
//            OutputStream out = new FileOutputStream(file);
//
//            IOUtils.copy(jarInputStream, out);
//            out.flush();
//            out.close();

            JarEntry classEntry = JarClassLoader.findClassEntry(jarInputStream, className);


            // Create the necessary package if needed...
            int index = className.lastIndexOf('.');
            if (index >= 0) {
                String packageName = className.substring(0, index);
                if (getPackage(packageName) == null) {
                    definePackage(packageName, "", "", "", "", "", "", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Ищет нужный класс в jar-файле
     *
     * @param jarInputStream
     * @param className
     * @return
     * @throws IOException
     */
    private static JarEntry findClassEntry(JarInputStream jarInputStream, String className) throws IOException {
        if (jarInputStream == null || className == null || className.isEmpty()) {
            return null;
        }
        while (true) {
            JarEntry jarEntry = jarInputStream.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            if (className.equals(jarEntry.getName().replace("/", "."))) {
                logger.info(JarClassLoaderUtils.getFileName(jarEntry.getName()) + " was found");
                return jarEntry;
            }
        }
        return null;
    }

    public static void unzipJar(String destinationDir, String jarPath) throws IOException {
        File file = new File(jarPath);
        JarFile jar = new JarFile(file);
        // fist get all directories,
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (fileName.endsWith("/")) {
                f.mkdirs();
            }

        }
        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();
            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);
            if (!fileName.endsWith("/")) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);

                // write contents of 'is' to 'fos'
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }
    }

    /**
     * Ищет первый jar - в jar файле
     *
     * @param jarFile
     * @param entryName
     * @return
     * @throws IOException
     */
    private static JarEntry findJarEntry(JarFile jarFile, String entryName) throws IOException {
        if (entryName != null && jarFile != null) {
            Enumeration entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                logger.info(entry.getName());
                if (entryName.equals(JarClassLoaderUtils.getFileName(entry.getName()))) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * Метод(тестовый) загружает класс по имени из jar файла
     *
     * @param jarFilePath
     * @param className
     * @return
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public static Class<?> loadClassFromJarFile(String jarFilePath, String className) throws MalformedURLException, ClassNotFoundException {
        logger.info("================loadClassFromJarFile===============");
        File file = new File(jarFilePath);
        URL url = file.toURL();
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);
        logger.info("JarClassLoader search " + className);
        return cl.loadClass(className);
    }

    /**
     * Метод(тестовый) проходится по jar файлу и выдает имена файлов, которые соответствуют постфиксу
     *
     * @param jarName
     * @param suffix
     * @return
     */
    @Deprecated
    public static List<String> getNames(String jarName, String suffix) {
        logger.info("================getClassesNames===============");
        logger.info("Search with postfix : " + suffix + " in file " + jarName);
        List<String> listNames = new ArrayList<String>();
        try {
            JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;

            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if (jarEntry.getName().endsWith(suffix)) {
                    listNames.add(jarEntry.getName());
                    logger.info(jarEntry.getName() + " was found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listNames;
    }
}
