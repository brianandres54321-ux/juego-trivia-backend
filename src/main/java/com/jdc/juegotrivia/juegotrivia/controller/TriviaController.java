package com.jdc.juegotrivia.juegotrivia.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.model.Respuesta;
import com.jdc.juegotrivia.juegotrivia.service.PartidaService;
import com.jdc.juegotrivia.juegotrivia.service.PreguntaService;
import com.jdc.juegotrivia.juegotrivia.service.RespuestaService;

@Controller
public class TriviaController {

    // 🔹 Mapa de salas (código → jugadores y puntos)
    private Map<String, Map<String, Integer>> salas = new ConcurrentHashMap<>();

    // 🔹 Mapa de preguntas por sala
    private Map<String, List<Pregunta>> preguntasPorSala = new ConcurrentHashMap<>();

    // 🔹 Estado de las salas (para evitar duplicar juegos)
    private Map<String, Boolean> estadoSala = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PreguntaService preguntaService;

    @Autowired
    private RespuestaService respuestaService;

    // Jugadores conectados con sus puntos
    private Map<String, Integer> jugadores = new ConcurrentHashMap<>();

    // Respuestas recibidas para la pregunta actual
    private Map<String, RespuestaJugador> respuestasActuales = new ConcurrentHashMap<>();

    private boolean juegoEnCurso = false;
    private int preguntaActualIndex = 0;
    private List<Pregunta> preguntasDelJuego;
    @Autowired
    private PartidaService partidaService;

    @MessageMapping("/nuevoJugador")
    public void nuevoJugador(@Payload Map<String, String> datos) {
        String nombre = datos.get("nombre");
        String codigo = datos.getOrDefault("codigo", "PUBLICA-0"); // pública o privada

        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("❌ Nombre de jugador inválido");
            return;
        }

        // Crear la sala si no existe
        salas.putIfAbsent(codigo, new ConcurrentHashMap<>());
        Map<String, Integer> jugadoresSala = salas.get(codigo);

        // Agregar jugador si no existe
        jugadoresSala.putIfAbsent(nombre, 0);

        System.out.println("✅ Jugador '" + nombre + "' unido a la sala: " + codigo);

        // Preparar lista actualizada
        List<JugadorInfo> listaJugadores = new ArrayList<>();
        jugadoresSala.forEach((n, puntos) -> listaJugadores.add(new JugadorInfo(n, puntos)));

        // Enviar SOLO a los jugadores de esa sala
        messagingTemplate.convertAndSend("/topic/jugadoresActualizados/" + codigo, listaJugadores);
    }

    @MessageMapping("/iniciarJuego")
    public void iniciarJuego(@Payload Map<String, String> datos) {
        String codigo = datos.getOrDefault("codigo", "PUBLICA-0");

        if (estadoSala.getOrDefault(codigo, false)) {
            System.out.println("⚠️ Ya hay un juego en curso en la sala " + codigo);
            return;
        }

        Map<String, Integer> jugadoresSala = salas.get(codigo);
        if (jugadoresSala == null || jugadoresSala.isEmpty()) {
            System.out.println("⚠️ No hay jugadores en la sala " + codigo);
            return;
        }

        estadoSala.put(codigo, true);

        try {
            List<Pregunta> preguntas = cargarPreguntasParaSala(codigo);
            if (preguntas == null || preguntas.isEmpty()) {
                System.out.println("⚠️ No hay preguntas disponibles para la sala " + codigo);
                estadoSala.put(codigo, false);
                messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigo,
                        Map.of("estado", "error", "mensaje", "No hay preguntas disponibles para esta sala"));
                return;
            }

            Collections.shuffle(preguntas);
            preguntasPorSala.put(codigo, preguntas);
            messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigo,
                    Map.of("estado", "iniciado", "totalPreguntas", preguntas.size()));
            new Thread(() -> enviarPreguntas(codigo)).start();

        } catch (Exception e) {
            System.out.println("❌ Error iniciando juego en sala " + codigo + ": " + e.getMessage());
            e.printStackTrace();
            estadoSala.put(codigo, false);
            messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigo,
                    Map.of("estado", "error", "mensaje", e.getMessage()));
        }
    }

    List<Pregunta> cargarPreguntasParaSala(String codigo) {
        if (codigo.startsWith("PUBLICA-")) {
            Long idCategoria = null;
            try {
                idCategoria = Long.parseLong(codigo.split("-")[1]);
            } catch (Exception ignored) {
            }

            if (idCategoria != null) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(idCategoria);
                List<Pregunta> preguntasPublicas = preguntaService.findByCategoriaAndPartidaIsNull(categoria);
                System.out.println("📚 Cargando preguntas públicas de la categoría " + idCategoria + ": "
                        + preguntasPublicas.size());
                if (!preguntasPublicas.isEmpty()) {
                    return preguntasPublicas;
                }

                List<Pregunta> preguntasCategoria = preguntaService.findByCategoria(categoria);
                System.out.println("📚 No había preguntas públicas; usando preguntas de la categoría " + idCategoria
                        + ": " + preguntasCategoria.size());
                return preguntasCategoria;
            }

            List<Pregunta> todas = preguntaService.findAll();
            System.out.println("📚 Código PUBLICA- mal formado, cargando todas las preguntas: " + todas.size());
            return todas;
        }

        var partidaOpt = partidaService.findByCodigo(codigo);
        if (partidaOpt.isPresent()) {
            Partida partida = partidaOpt.get();
            List<Pregunta> preguntasPartida = preguntaService.findByPartida(partida);
            System.out.println("📚 Cargando preguntas de la partida privada (codigo=" + partida.getCodigo() + "): "
                    + preguntasPartida.size());
            return preguntasPartida;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró una partida con código " + codigo);
    }

    private void enviarPreguntas(String codigo) {
        try {
            List<Pregunta> preguntas = preguntasPorSala.get(codigo);

            if (preguntas == null || preguntas.isEmpty()) {
                System.out.println("⚠️ No hay preguntas en la sala " + codigo);
                return;
            }

            Thread.sleep(2000);

            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta pregunta = preguntas.get(i);
                List<Respuesta> respuestas = respuestaService.findByPregunta(pregunta);
                if (respuestas.isEmpty())
                    continue;

                Collections.shuffle(respuestas);

                PreguntaDTO dto = new PreguntaDTO(
                        pregunta.getIdPregunta(),
                        pregunta.getTexto(),
                        respuestas,
                        i + 1,
                        preguntas.size(),
                        15);

                System.out.println("📤 Enviando pregunta " + (i + 1) + " a sala " + codigo);
                messagingTemplate.convertAndSend("/topic/pregunta/" + codigo, dto);

                Thread.sleep(15000);
            }

            finalizarJuego(codigo);

        } catch (InterruptedException e) {
            System.out.println("❌ Error en envío de preguntas: " + e.getMessage());
            e.printStackTrace();
            estadoSala.put(codigo, false);
        }
    }

    @MessageMapping("/responder")
    public void procesarRespuesta(@Payload Map<String, Object> datos) {
        try {
            String nombreJugador = (String) datos.get("jugador");
            String codigoSala = (String) datos.getOrDefault("codigoSala", "PUBLICA-0");
            Long idRespuesta = Long.parseLong(datos.get("idRespuesta").toString());
            long tiempoRespuesta = Long.parseLong(datos.get("tiempo").toString());

            // 🔹 Verificar si existe la sala
            Map<String, Integer> jugadoresSala = salas.get(codigoSala);
            if (jugadoresSala == null) {
                System.out.println("⚠️ No existe la sala: " + codigoSala);
                return;
            }

            // 🔹 Buscar la respuesta seleccionada
            Optional<Respuesta> opt = respuestaService.findById(idRespuesta);
            if (opt.isEmpty()) {
                System.out.println("⚠️ Respuesta no encontrada (ID: " + idRespuesta + ")");
                return;
            }

            Respuesta respuesta = opt.get();
            boolean correcta = respuesta.isCorrecta();
            int puntosGanados = 0;

            if (correcta) {
                long tiempoMaximo = 15000; // 15 segundos
                double factorVelocidad = Math.max(0, (tiempoMaximo - tiempoRespuesta) / (double) tiempoMaximo);

                // 🔹 Cálculo de puntos: base 500 + bonus por velocidad (hasta +500)
                puntosGanados = 500 + (int) (500 * factorVelocidad);

                // 🔹 Sumar puntos al jugador dentro de su sala
                jugadoresSala.put(nombreJugador,
                        jugadoresSala.getOrDefault(nombreJugador, 0) + puntosGanados);

                System.out.println("✅ " + nombreJugador + " respondió CORRECTO (+"
                        + puntosGanados + " pts) en sala " + codigoSala);
            } else {
                System.out.println("❌ " + nombreJugador + " respondió INCORRECTO en sala " + codigoSala);
            }

        } catch (Exception e) {
            System.out.println("❌ Error procesando respuesta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enviarResultados(Pregunta pregunta) {
        // Obtener respuesta correcta
        List<Respuesta> respuestas = respuestaService.findByPregunta(pregunta);
        String respuestaCorrecta = respuestas.stream()
                .filter(Respuesta::isCorrecta)
                .map(Respuesta::getTexto)
                .findFirst()
                .orElse("");

        // Preparar ranking actual
        List<JugadorInfo> ranking = new ArrayList<>();
        jugadores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> ranking.add(new JugadorInfo(entry.getKey(), entry.getValue())));

        ResultadoDTO resultado = new ResultadoDTO(
                respuestaCorrecta,
                new ArrayList<>(respuestasActuales.values()),
                ranking);

        System.out.println("📤 Enviando resultados de la pregunta");
        messagingTemplate.convertAndSend("/topic/resultado", resultado);
    }

    private void finalizarJuego(String codigo) {
        Map<String, Integer> jugadoresSala = salas.get(codigo);
        if (jugadoresSala == null)
            return;

        List<JugadorInfo> rankingFinal = jugadoresSala.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> new JugadorInfo(e.getKey(), e.getValue()))
                .toList();

        System.out.println("🏁 Fin del juego en sala " + codigo + " (" + rankingFinal.size() + " jugadores)");

        messagingTemplate.convertAndSend("/topic/finJuego/" + codigo, rankingFinal);
        estadoSala.put(codigo, false);
    }

    // DTOs internos
    public static class JugadorInfo {
        public String nombre;
        public int puntos;

        public JugadorInfo(String nombre, int puntos) {
            this.nombre = nombre;
            this.puntos = puntos;
        }
    }

    public static class PreguntaDTO {
        public Long id;
        public String texto;
        public List<RespuestaSimple> respuestas;
        public int numeroPregunta;
        public int totalPreguntas;
        public int tiempoLimite;

        public PreguntaDTO(Long id, String texto, List<Respuesta> respuestasCompletas,
                int numeroPregunta, int totalPreguntas, int tiempoLimite) {
            this.id = id;
            this.texto = texto;
            this.numeroPregunta = numeroPregunta;
            this.totalPreguntas = totalPreguntas;
            this.tiempoLimite = tiempoLimite;

            this.respuestas = new ArrayList<>();
            for (Respuesta r : respuestasCompletas) {
                this.respuestas.add(new RespuestaSimple(r.getIdRespuesta(), r.getTexto()));
            }
        }
    }

    public static class RespuestaSimple {
        public Long id;
        public String texto;

        public RespuestaSimple(Long id, String texto) {
            this.id = id;
            this.texto = texto;
        }
    }

    public static class RespuestaJugador {
        public String jugador;
        public boolean correcta;
        public int puntos;
        public long tiempo;

        public RespuestaJugador(String jugador, boolean correcta, int puntos, long tiempo) {
            this.jugador = jugador;
            this.correcta = correcta;
            this.puntos = puntos;
            this.tiempo = tiempo;
        }
    }

    public static class ResultadoDTO {
        public String respuestaCorrecta;
        public List<RespuestaJugador> respuestas;
        public List<JugadorInfo> ranking;

        public ResultadoDTO(String respuestaCorrecta, List<RespuestaJugador> respuestas,
                List<JugadorInfo> ranking) {
            this.respuestaCorrecta = respuestaCorrecta;
            this.respuestas = respuestas;
            this.ranking = ranking;
        }
    }
}
