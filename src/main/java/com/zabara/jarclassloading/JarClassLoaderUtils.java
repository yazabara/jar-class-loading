package com.zabara.jarclassloading;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by Yaroslav on 03.08.2014.
 */
public class JarClassLoaderUtils {

    public static void writeClassInfo(Writer writer, Class clazz) throws IOException {
        writer.write("Class name: " + clazz.getName() + "\n");
        writer.write("Package name: " + clazz.getPackage() + "\n");
        writer.write("ClassLoader " + clazz.getClassLoader().toString() + "\n");
        writer.write("======================================" + "\n");
        writer.write("Constructors" + "\n");
        int index = 0;
        for (Constructor constructor : clazz.getConstructors()) {
            writer.write((index++) + ") " + constructor + "\n");
        }
        writer.write("======================================" + "\n");
        writer.write("All methods:" + "\n");
        index = 0;
        for (Method method : clazz.getMethods()) {
            writer.write((index++) + ") " + method + "\n");
        }
        writer.write("======================================" + "\n");
        writer.write("Declared methods" + "\n");
        index = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            writer.write((index++) + ") " + method + "\n");
        }
    }
}
