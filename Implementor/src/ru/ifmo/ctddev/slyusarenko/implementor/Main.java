package ru.ifmo.ctddev.slyusarenko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;

/**
 * @author Maxim Slyusarenko
 * @version 1.0
 * @see ru.ifmo.ctddev.slyusarenko.implementor.Implementor
 */
public class Main {


    /**
     * Check given args and call method {@link ru.ifmo.ctddev.slyusarenko.implementor.Implementor#implement Implementor.implement} or {@link ru.ifmo.ctddev.slyusarenko.implementor.Implementor#implementJar Implementor.implementJar}
     * @param args arguments with information about given class and which method we must call
     * @see ru.ifmo.ctddev.slyusarenko.implementor.Implementor
     * @version 1.0
     * @since 1.0
     * @author Maxim Slyusarenko
     */
    public static void main(String[] args) {
        String className;
        boolean generateJar = false;
        if (args != null && args.length == 1 && args[0] != null) {
            className = args[0];
            generateJar = false;
        } else if (args != null && args.length == 2 && args[0] != null && args[1] != null && args[0].equals("-jar")) {
            className = args[1];
            generateJar = true;
        } else {
            System.err.println("Incorrect args");
            return;
        }
        try {
            Implementor implementor = new Implementor();
            if (generateJar) {
                implementor.implementJar(Class.forName(className), new File((".")));
            } else {
                implementor.implement(Class.forName(className), new File("."));
            }
        } catch (ImplerException e) {
            System.err.println("Impler exception: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("class " + className + " not found");
        }
    }
}
