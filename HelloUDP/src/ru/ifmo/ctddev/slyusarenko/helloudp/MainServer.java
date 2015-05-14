package ru.ifmo.ctddev.slyusarenko.helloudp;

public class MainServer {

    public static void main(String[] args) {
        HelloUDPServer server = new HelloUDPServer();
        if (args != null && args[0] != null && args[1] != null) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } else {
            System.err.println("Incorrect arguments");
        }
    }
}
