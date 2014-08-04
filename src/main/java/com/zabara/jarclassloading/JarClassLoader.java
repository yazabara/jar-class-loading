package com.zabara.jarclassloading;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

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
                    clazz = findClass(jarPath, name);
                }
            }
        }
        return clazz;//super.loadClass(name, resolve);
    }

    public Class<?> findClass(String jarFilePath, String className) {
        Class result = null;
        try {

            JarFile jarFile = new JarFile(jarFilePath);
            File jarDir = new File(jarFilePath).getParentFile();

            JarClassLoader.unzipJar(jarDir + "/" + className, jarFilePath);

            File unJarDir = new File(jarDir + "/" + className);

            List<File> files = JarClassLoader.getJarFiles(unJarDir);

            // Create the necessary package if needed...
            int index = className.lastIndexOf('.');
            if (index >= 0) {
                String packageName = className.substring(0, index);
                if (getPackage(packageName) == null) {
                    definePackage(packageName, "", "", "", "", "", "", null);
                }
            }


            for (File file: files) {
                JarInputStream inputStream = new JarInputStream(new FileInputStream(file.getAbsolutePath()));
                JarEntry jarEntry = JarClassLoader.findClassEntry(inputStream, className);
                //если не null - это наш класс
                if (jarEntry != null) {
                    try {
                        JarFile jarFile1 = new JarFile(file);
                        InputStream is = jarFile1.getInputStream(jarEntry);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        org.apache.commons.io.IOUtils.copy(is, os);
                        byte[] bytes = os.toByteArray();
                        result = defineClass(className, bytes, 0, bytes.length);
                    } catch (IOException ioe) {
                    }
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Проходит по всем директориям и возвращает нужные файлы
     *
     * @param unJarDir
     * @return
     */
    private static List<File> getJarFiles(File unJarDir) {
        List<File> files = new ArrayList<File>(Arrays.asList(unJarDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".jar"); // return .url files
            }
        })));

        List<File> subdirs = Arrays.asList(unJarDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        }));

        for (File subdir : subdirs) {
            files.addAll(JarClassLoader.getJarFiles(subdir));
        }
        return files;
    }

    private static void writeFile(String fileName, InputStream is) throws IOException {
        if (!fileName.endsWith("/")) {
            File f = new File(fileName);
            FileOutputStream fos = new FileOutputStream(f);
            while (is.available() > 0) {
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
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
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();
            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);
            if (fileName.endsWith("/")) {
                f.mkdirs();
            }
        }
        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();
            InputStream is = jar.getInputStream(entry);
            String fileName = destinationDir + File.separator + entry.getName();
            JarClassLoader.writeFile(fileName, is);
        }
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
