package ru.ifmo.ctddev.slyusarenko.helloudp;

public class MainClient {

    public static void main(String[] args) {
        HelloUDPClient client = new HelloUDPClient();
        if (args != null && args.length == 5 && args[0] != null && args[1] != null && args[2] != null && args[3] != null && args[4] != null) {
            client.start(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } else {
            System.err.println("Incorrect arguments");
        }
    }
}
