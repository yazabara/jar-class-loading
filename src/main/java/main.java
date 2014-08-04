import com.zabara.jarclassloading.JarClassLoader;
import com.zabara.jarclassloading.JarClassLoaderUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * Created by Yaroslav on 03.08.2014.
 */
public class Main {

    public static void main(String[] args) {
        try {
            Class clazz = JarClassLoader.loadJars("D:\\ElenaF.jar", "task1.Elena");
            Writer writer = new OutputStreamWriter(System.out);
            JarClassLoaderUtils.writeClassInfo(writer, clazz);

            Class[] argTypes = new Class[]{String.class, String.class};
            Method main = clazz.getDeclaredMethod("jsouObraceneRetezce", argTypes);
            Boolean result = (Boolean) main.invoke(null, "1234567890", "0987654321");

            writer.write("result is " + result);

            writer.flush();
            writer.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
