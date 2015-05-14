package ru.ifmo.ctddev.slyusarenko.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Server works using UDP protocol
 * @author Maxim Slyusarenko
 * @version 1.0
 */
public class HelloUDPServer implements HelloServer {

    private final List<Pair<DatagramSocket, List<Thread>>> sockets = new ArrayList<>();
    private static final String HELLO = "Hello, ";
    private static final int TIMEOUT = 500;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Start server with given arguments
     * @param port port on which server will receive requests
     * @param threads number of threads on which server wil work
     */
    @Override
    public void start(int port, int threads) {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            List<Thread> thread = new ArrayList<>();
            socket.setSoTimeout(TIMEOUT);
            for (int i = 0; i < threads; i++) {
                thread.add(new Thread(new ServerWorker(socket)));
            }
            synchronized (sockets) {
                sockets.add(new Pair<>(socket, thread));
                for (int i = 0; i < threads; i++) {
                    thread.get(i).start();
                }
            }
        } catch (IOException e) {
            System.err.println("Exception " + e.getMessage());
        }
    }

    /**
     * Interrupt all working threads
     */
    @Override
    public synchronized void close() {
        for (Pair<DatagramSocket, List<Thread>> pair: sockets) {
            pair.getValue().forEach(java.lang.Thread::interrupt);
        }
        try {
            for (Pair<DatagramSocket, List<Thread>> pair: sockets) {
                for (Thread thread: pair.getValue()) {
                    thread.join();
                }
            }
        } catch (InterruptedException ignore) {}
        for (Pair<DatagramSocket, List<Thread>> socket: sockets) {
            socket.getKey().close();
        }
        sockets.clear();
    }

    private class ServerWorker implements Runnable {

        private final DatagramSocket socketToReceive;

        public ServerWorker(DatagramSocket socketToReceive) {
            this.socketToReceive = socketToReceive;
        }

        @Override
        public void run() {
            DatagramPacket packetToSend = new DatagramPacket(new byte[1024], 1024);
            try {
                packetToSend = new DatagramPacket(new byte[socketToReceive.getReceiveBufferSize()], socketToReceive.getReceiveBufferSize());
            } catch (SocketException ignored) {}
            try (DatagramSocket socketToSend = new DatagramSocket()) {
                while (!Thread.interrupted()) {
                    try {
                        socketToReceive.receive(packetToSend);
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    String response = HELLO + new String(packetToSend.getData(), packetToSend.getOffset(), packetToSend.getLength(), CHARSET);
                    byte[] buf = response.getBytes(CHARSET);
                    packetToSend = new DatagramPacket(buf, buf.length, packetToSend.getAddress(), packetToSend.getPort());
                    socketToSend.send(packetToSend);
                }
            } catch (IOException e) {
                System.err.println("Exception: " + e.getMessage());
            }
        }
    }
}