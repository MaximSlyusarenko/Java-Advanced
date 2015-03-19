package ru.ifmo.ctddev.slyusarenko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Constructor;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @see ru.ifmo.ctddev.slyusarenko.implementor.Main Main class
 */

public class Implementor implements info.kgeorgiy.java.advanced.implementor.JarImpler {


    /**
     * True if needs to generate .jar file and false otherwise
     */
    private static boolean generateJar;

    /**
     * Get returned value for element which has type Class
     * @param clazz is a class we get returned value for
     * @return Returned value for {@code clazz}
     * @see java.lang.Class
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    private static Object getValue(Class clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(boolean.class)) {
                return false;
            } else if (clazz.equals(void.class)) {
                return "";
            } else {
                return 0;
            }
        } else {
            return null;
        }
    }

    /**
     * Write parameters of method or constructor and exceptions which can be thrown by this method or constructor
     * @param params are parameters of method or constructor
     * @param exceptions are exceptions thrown by method or constructor
     * @param writer is writes to file
     * @throws IOException if {@link java.io.Writer#write Writer.write} throws it
     * @see java.lang.Class
     * @see java.io.Writer
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    private static void printParametersAndExceptions(Class[] params, Class[] exceptions, Writer writer) throws IOException {
        writer.write("(");
        if (params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                if (i != params.length - 1) {
                    writer.write(params[i].getTypeName() + " args" + Integer.toString(i) + ", ");
                } else {
                    writer.write(params[i].getTypeName() + " args" + Integer.toString(i) + ")");
                }
            }
        } else {
            writer.write(")");
        }
        if (exceptions.length > 0) {
            writer.write(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                if (i != exceptions.length - 1) {
                    writer.write(exceptions[i].getTypeName() + ", ");
                } else {
                    writer.write(exceptions[i].getTypeName());
                }
            }
        }
    }

    /**
     * <p>Write name of given constructor, it's parameters and exceptions which can be thrown</p>
     * <p>Uses {@link #printParametersAndExceptions printParametersAndExceptions} to print parameters and exceptions</p>
     * <p>Uses {@link java.lang.reflect.Constructor#getParameterTypes Constructor.getParameterTypes} to get types of given constructor's parameters</p>
     * <p>Uses {@link java.lang.reflect.Constructor#getExceptionTypes Constructor.getExceptionTypes} to get exceptions which can be thrown by given {@code constructor}</p>
     * @param constructor is a given constructor
     * @param className is a name of given class. This name uses to write our constructor name
     * @param writer writes to file
     * @throws IOException if {@link java.io.Writer#write Writer.write} throws it
     * @see java.lang.reflect.Constructor
     * @see java.io.Writer
     * @see java.lang.reflect.Modifier
     * @see java.lang.Class
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    private static void printConstructor(Constructor constructor, String className, Writer writer) throws IOException {
        int modifiers = constructor.getModifiers();
        if (Modifier.isPrivate(modifiers)) {
            return;
        }
        if (Modifier.isTransient(modifiers)) {
            modifiers -= Modifier.TRANSIENT;
        }
        writer.write(Modifier.toString(modifiers) + " " + className + "Impl");
        Class[] parameters = constructor.getParameterTypes();
        Class[] exceptions = constructor.getExceptionTypes();
        printParametersAndExceptions(parameters, exceptions, writer);
        writer.write("{\n\tsuper(");
        if (parameters.length != 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (i != parameters.length - 1) {
                    writer.write("args" + Integer.toString(i) + ", ");
                } else {
                    writer.write("args" + Integer.toString(i) + ");\n}\n\n");
                }
            }
        } else {
            writer.write(");\n}\n\n");
        }
    }

    /**
     * <p>Write name of specified method, it's parameters and exceptions which can be thrown</p>
     * <p>Uses {@link #printParametersAndExceptions printParametersAndExceptions} to print parameters and exceptions</p>
     * <p>Uses {@link java.lang.reflect.Method#getParameterTypes Method.getParameterTypes} to get types of specified method's parameters</p>
     * <p>Uses {@link java.lang.reflect.Method#getExceptionTypes Method.getExceptionTypes} to get exceptions which can be thrown by specified {@code method}</p>
     * @param method is a specified method
     * @param writer writes to file
     * @see java.io.Writer
     * @see java.lang.reflect.Method
     * @see java.lang.reflect.Modifier
     * @see java.lang.Class
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    private static void printMethod(Method method, Writer writer) {
        int modifiers = method.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            modifiers -= Modifier.ABSTRACT;
        }
        if (Modifier.isTransient(modifiers)) {
            modifiers -= Modifier.TRANSIENT;
        }
        try {
            writer.write(Modifier.toString(modifiers) + " " + method.getReturnType().getTypeName() + " " + method.getName());
            Class[] params = method.getParameterTypes();
            Class[] exceptions = method.getExceptionTypes();
            printParametersAndExceptions(params, exceptions, writer);
            writer.write(" {\n\t return ");
            if (getValue(method.getReturnType()) == null) {
                writer.write("null;\n}\n\n");
            } else {
                writer.write(getValue(method.getReturnType()).toString() + ";\n}\n\n");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * <p>Implement specified class or interface {@code token} Creates new directory {@code root} if it isn't exists.</p>
     * <p>Uses {@link #printMethod printMethod} to print methods of specified class or interface</p>
     * <p>Uses {@link #printConstructor printConstructor} to print constructors of specified class</p>
     * @param token is an information about class or interface which method implements
     * @param root is a directory which method creates if it isn't exists
     * @throws ImplerException if {@code token == null} or {@code root == null} or {@code token} is a primitive or final or synthetic class or {@code token} is an Array
     * @see java.lang.Class
     * @see java.lang.reflect.Method
     * @see java.lang.reflect.Constructor
     * @see java.io.File
     * @see info.kgeorgiy.java.advanced.implementor.ImplerException
     * @see info.kgeorgiy.java.advanced.implementor.Impler
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */

    @Override
    public void implement(Class<?> token, File root) throws ImplerException {
        if (token == null || root == null || token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token.isSynthetic()) {
            throw new ImplerException("Wrong input");
        }
        String canonicalName = token.getCanonicalName();
        String className = token.getSimpleName();
        String outputName = className + "Impl.java";
        String packageName = token.getPackage().getName();
        String path = token.getCanonicalName().replace(".", File.separator) + "Impl.java";
        int k = path.lastIndexOf(File.separator);
        root = new File(root, path.substring(0, k));
        root.mkdirs();
        File file = new File(root, outputName);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            writer.write("package " + packageName + ";\n");
            try {
                Class clazz = Class.forName(canonicalName);
                if (clazz.isInterface()) {
                    writer.write("public class " + className + "Impl implements " + className + " {\n");
                    Method[] methods = clazz.getMethods();
                    for (Method method: methods) {
                        int modifiers = method.getModifiers();
                        if (Modifier.isAbstract(modifiers)) {
                            printMethod(method, writer);
                        }
                    }
                    writer.write("\n}");
                } else {
                    writer.write("public class " + className + "Impl extends " + className + " {\n");
                    boolean allConstructorsArePrivate = true;
                    Constructor[] constructors = clazz.getDeclaredConstructors();
                    for (Constructor constructor: constructors) {
                        if (!Modifier.isPrivate(constructor.getModifiers())) {
                            allConstructorsArePrivate = false;
                        }
                        printConstructor(constructor, className, writer);
                    }
                    if (allConstructorsArePrivate) {
                        throw new ImplerException("All constructors are private");
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method: methods) {
                        int modifiers = method.getModifiers();
                        if (Modifier.isAbstract(modifiers)) {
                            printMethod(method, writer);
                        }
                    }
                    methods = clazz.getDeclaredMethods();
                    for (Method method: methods) {
                        int modifiers = method.getModifiers();
                        if (Modifier.isAbstract(modifiers) && !Modifier.isPublic(modifiers)) {
                            printMethod(method, writer);
                        }
                    }
                    boolean abstractSuperClass = true;
                    while (abstractSuperClass) {
                        abstractSuperClass = false;
                        clazz = clazz.getSuperclass();
                        methods = clazz.getDeclaredMethods();
                        for (Method method: methods) {
                            int mods = method.getModifiers();
                            if (Modifier.isAbstract(mods) && !Modifier.isPublic(mods)) {
                                abstractSuperClass = true;
                                printMethod(method, writer);
                            }
                        }
                    }
                    writer.write("\n}");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Class " + className + " not found");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Compiles <tt>source</tt> file in <tt>root</tt> directory
     * @param root root directory
     * @param source source <tt>.java</tt> file
     * @throws ImplerException when compilation error happens
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    private void compileFile(File root, File source) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.add(source.getPath());
        args.add("-cp");
        args.add(root.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (exitCode != 0) {
            if (!source.exists()) {
                throw new ImplerException("file " + source.toString() + " not exists");
            }
            throw new ImplerException("Compilation error");
        }
    }

    /**
     * Creates for <tt>clazz</tt> in <tt>root</tt> directory
     * @param clazz class token to create jar for
     * @param root root directory
     * @throws ImplerException when {@link #add} throws {@link java.io.IOException}
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    private void createJarFile(Class<?> clazz, File root) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File jar = new File(root, clazz.getSimpleName() + "Impl.jar");
        File path = new File(clazz.getPackage().getName().split("\\.")[0]);
        try (JarOutputStream stream = new JarOutputStream(new FileOutputStream(jar), manifest)) {
            add(path, stream);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Adds <tt>file</tt> in <tt>stream</tt>
     *
     * @param file file to be added
     * @param stream jar stream in which file must be added
     * @throws IOException if it's thrown while reading <tt>file</tt>
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */

    private void add(File file, JarOutputStream stream) throws IOException {
        if (file.isDirectory()) {
            JarEntry entry = new JarEntry(file.getPath());
            stream.putNextEntry(entry);
            stream.closeEntry();
            try (DirectoryStream<Path> stream1 = Files.newDirectoryStream(Paths.get(file.getPath()))) {
                for (Path path: stream1) {
                    add(path.toFile(), stream);
                }
            }
        } else if (file.getName().endsWith(".class")) {
            JarEntry entry = new JarEntry(file.getPath());
            stream.putNextEntry(entry);
            BufferedInputStream buff = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = buff.read(buffer)) != -1) {
                stream.write(buffer, 0, count);
            }
            stream.closeEntry();
        }
    }

    /**
     * Call method implement and then creates jar file for implemented class
     *
     * @param aClass is an information about class or interface which method implements
     * @param file root directory to create file in
     * @throws ImplerException when {@link #implement implement} or {@link #compileFile compileFile} or {@link #createJarFile createJarFile}throws it
     * @see #implement implement
     * @see #compileFile compileFile
     * @see #createJarFile createJarFile
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    @Override
    public void implementJar(Class<?> aClass, File file) throws ImplerException {
        implement(aClass, file);
        compileFile(file, new File(file, aClass.getCanonicalName().replace(".", File.separator) + "Impl.java"));
        createJarFile(aClass, file);
    }

}
