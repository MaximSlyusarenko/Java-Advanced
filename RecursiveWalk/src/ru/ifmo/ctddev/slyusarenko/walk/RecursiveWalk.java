package ru.ifmo.ctddev.slyusarenko.walk;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {

    private static int getHash(String fileName) {
        int hash;
        try (InputStream is = new FileInputStream(fileName)) {
            hash = 0x811c9dc5;
            int c;
            while ((c = is.read()) >= 0) {
                hash = (hash * 0x01000193) ^ (c & 0xff);
            }
        } catch(IOException e) {
            return 0;
        }
        return hash;
    }

    private static void write(String path, int hash, Writer writer) {
        try {
            writer.write(String.format("%08x", hash) + " " + path + "\n");
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void walk(String addr, Writer writer) {
        File file = new File(addr);
        if (file.isDirectory()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(file.getPath()))) {
                for (Path path: stream) {
                    walk(path.toString(), writer);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            write(file.toString(), getHash(file.toString()), writer);
        }
    }

    public static void main(String[] args) {
        String inputName;
        String outputName;
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect args");
        } else {
            inputName = args[0];
            outputName = args[1];
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(inputName), "UTF-8");
                 BufferedReader buff = new BufferedReader(reader)) {
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputName), "UTF-8")) {
                    String c;
                    while ((c = buff.readLine()) != null) {
                        walk(c, writer);
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("Can't write to output file");
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            } catch (FileNotFoundException e) {
                System.err.println("Input File not found");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
