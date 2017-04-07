package com.klymenko.classloader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by klymenko.ruslan on 07.04.2017.
 */
public class EmptyTypeClassLoader extends ClassLoader {

    private final String URL_PATH;

    private static final String EXT_JAVA = ".java";
    private static final String EXT_CLASS = ".class";

    public EmptyTypeClassLoader() {
        URL_PATH = "./";
    }

    public EmptyTypeClassLoader(String urlPath) {
        URL_PATH = urlPath;
    }

    @Override
    public Class<?> loadClass(String className) {
        try {
            return this.findClass(className);
        } catch (ClassNotFoundException e) {
            return loadEmtpyClass(className);
        }
    }
    private Class loadEmtpyClass(String className) {
        String classBody = createEmptyClassBody(className);
        String filePath = URL_PATH + className + EXT_JAVA;
        try {
            File file = createFile(classBody, filePath);
            compileClassFile(file);
            return loadClassByUrlAndName(URL_PATH, className);
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            File javaFile = new File(filePath);
            javaFile.delete();
            File classFile = new File(filePath.replace(EXT_JAVA, EXT_CLASS));
            classFile.delete();
        }
    }

    private String createEmptyClassBody(String className) {
        return "public class " + className + " {}";
    }

    private File createFile(String classBody, String filePath) throws IOException {
        Files.write(Paths.get(filePath), classBody.getBytes());
        return new File(filePath);
    }

    private void compileClassFile(File file) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> sources =
                manager.getJavaFileObjectsFromFiles(Arrays.asList(file));
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, null, null, sources);
        task.call();
    }

    private Class loadClassByUrlAndName(String urlPath, String className) {
        try {
            File file = new File(urlPath);
            URL[] urls = new URL[]{file.toURI().toURL()};
            ClassLoader cl = new URLClassLoader(urls);
            return cl.loadClass(className);
        }  catch (ClassNotFoundException | MalformedURLException e) {
            throw new RuntimeException();
        }
    }

    public static void main(String [] args) {
        EmptyTypeClassLoader emptyTypeClassLoader = new EmptyTypeClassLoader();
        Class emptyClass = emptyTypeClassLoader.loadClass("Empty");
        System.out.println(emptyClass.getCanonicalName());
    }

}
