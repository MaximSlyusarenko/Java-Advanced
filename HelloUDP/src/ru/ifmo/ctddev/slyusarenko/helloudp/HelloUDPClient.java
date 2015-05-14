package ru.ifmo.ctddev.slyusarenko.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Client works using UDP protocol
 *  @author Maxim Slyuarenko
 *  @version 1.0
 */
public class HelloUDPClient implements HelloClient {

    private static final int TIMEOUT = 500;

    /**
     * Start client with given arguments
     * @param ip IP-address
     * @param port port on which client send data
     * @param prefix prefix of requests
     * @param requests number of requests in thread
     * @param threads number of threads which will send packets
     */
    @Override
    public void start(String ip, int port, String prefix, int requests, int threads) {
        List<Thread> workThreads = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            workThreads.add(new Thread(new ClientWorker(ip, port, prefix, requests, i)));
            workThreads.get(i).start();
        }
        try {
            for (int i = 0; i < threads; i++) {
                workThreads.get(i).join();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private class ClientWorker implements Runnable {

        private final String ip;
        private final int port;
        private final String prefix;
        private final int requests;
        private final int threadNumber;

        public ClientWorker(String ip, int port, String prefix, int requests, int threadNumber) {
            this.ip = ip;
            this.port = port;
            this.prefix = prefix;
            this.requests = requests;
            this.threadNumber = threadNumber;
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT);
                byte[] receive = new byte[socket.getReceiveBufferSize()];
                DatagramPacket packetToReceive = new DatagramPacket(receive, receive.length);
                InetAddress inetAddress = InetAddress.getByName(ip);
                String message;
                for (int requestNumber = 0; requestNumber < requests; requestNumber++) {
                    message = prefix + threadNumber + "_" + requestNumber;
                    byte[] toSend = message.getBytes("UTF-8");
                    DatagramPacket packetToSend = new DatagramPacket(toSend, toSend.length, inetAddress, port);
                    while (true) {
                        socket.send(packetToSend);
                        try {
                            socket.receive(packetToReceive);
                            String receivedString = new String(packetToReceive.getData(), packetToReceive.getOffset(), packetToReceive.getLength(), "UTF-8");
                            System.out.println(message);
                            System.out.println(receivedString);
                            String mustReceive = "Hello, " + message;
                            if (receivedString.equals(mustReceive)) {
                                break;
                            }
                        } catch (SocketTimeoutException ignored) {
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception " + e.getMessage());
            }
        }
    }
}
