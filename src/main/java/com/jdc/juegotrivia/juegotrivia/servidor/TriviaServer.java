package com.jdc.juegotrivia.juegotrivia.servidor;

import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.service.JugadorService;
import com.jdc.juegotrivia.juegotrivia.service.PartidaService;
import com.jdc.juegotrivia.juegotrivia.service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.List;

@Component
public class TriviaServer {

    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;
    private static final int TCP_PORT = 5000;

    @Autowired
    private PreguntaService preguntaService;

    @Autowired
    private PartidaService partidaService;

    @Autowired
    private JugadorService jugadorService;

    private boolean juegoEnCurso = false;

    public void iniciarJuego() {
        if (juegoEnCurso) {
            System.out.println("⚠️ Ya hay un juego en curso.");
            return;
        }

        juegoEnCurso = true;
        System.out.println("🎮 Iniciando juego...");

        new Thread(this::iniciarMulticast).start();
        new Thread(this::iniciarTCPServer).start();
    }

    private void iniciarMulticast() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress grupo = InetAddress.getByName(MULTICAST_ADDRESS);
            List<Pregunta> preguntas = preguntaService.findAll();

            if (preguntas.isEmpty()) {
                System.out.println("⚠️ No hay preguntas disponibles.");
                juegoEnCurso = false;
                return;
            }

            for (Pregunta p : preguntas) {
                String mensaje = "Pregunta: " + p.getTexto();
                DatagramPacket paquete = new DatagramPacket(
                        mensaje.getBytes(),
                        mensaje.length(),
                        grupo,
                        MULTICAST_PORT
                );

                socket.send(paquete);
                System.out.println("📡 Enviada: " + mensaje);
                Thread.sleep(10000);
            }

            System.out.println("🏁 Fin del juego.");
            juegoEnCurso = false;

        } catch (Exception e) {
            e.printStackTrace();
            juegoEnCurso = false;
        }
    }

    private void iniciarTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("🧠 Servidor TCP escuchando en el puerto " + TCP_PORT);

            while (juegoEnCurso) {
                Socket clienteSocket = serverSocket.accept();
                new Thread(() -> manejarCliente(clienteSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manejarCliente(Socket socket) {
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String mensaje = entrada.readLine();
            System.out.println("💬 Respuesta recibida: " + mensaje);

            // Formato esperado: "jugador:Juan;respuesta:3"
            String[] partes = mensaje.split(";");
            String nombreJugador = partes[0].split(":")[1];
            int idRespuesta = Integer.parseInt(partes[1].split(":")[1]);

            boolean correcta = jugadorService.verificarRespuesta(nombreJugador, (long) idRespuesta);

            if (correcta) {
                salida.println("✅ Correcto");
            } else {
                salida.println("❌ Incorrecto");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
