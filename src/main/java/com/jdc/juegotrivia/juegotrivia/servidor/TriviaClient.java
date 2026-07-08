package com.jdc.juegotrivia.juegotrivia.servidor;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TriviaClient {

    private static final String MULTICAST_GROUP = "230.0.0.0";
    private static final int MULTICAST_PORT = 4446;
    private static final String SERVER_HOST = "localhost";
    private static final int TCP_PORT = 5000;

    public static void main(String[] args) {
        try {
            // Escuchar preguntas multicast
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            System.out.println("✅ Cliente conectado al grupo multicast " + MULTICAST_GROUP);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Recibir pregunta
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String pregunta = new String(packet.getData(), 0, packet.getLength());
                System.out.println("\n🧠 Pregunta recibida: " + pregunta);

                System.out.print("👉 Tu respuesta: ");
                String respuesta = scanner.nextLine();

                // Enviar respuesta al servidor por TCP
                try (Socket tcpSocket = new Socket(SERVER_HOST, TCP_PORT);
                     PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true)) {

                    out.println(respuesta);
                    System.out.println("📤 Respuesta enviada al servidor.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
