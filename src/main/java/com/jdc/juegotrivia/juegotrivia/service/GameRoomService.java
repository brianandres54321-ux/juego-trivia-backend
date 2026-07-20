package com.jdc.juegotrivia.juegotrivia.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.jdc.juegotrivia.juegotrivia.dto.EstadoJuegoDTO;
import com.jdc.juegotrivia.juegotrivia.dto.JugadorInfoDTO;
import com.jdc.juegotrivia.juegotrivia.dto.PreguntaDTO;
import com.jdc.juegotrivia.juegotrivia.dto.RespuestaOpcionDTO;
import com.jdc.juegotrivia.juegotrivia.dto.ResultadoDTO;
import com.jdc.juegotrivia.juegotrivia.game.SalaJuego;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.model.Respuesta;

import jakarta.annotation.PreDestroy;

/**
 * Motor del juego en vivo: crea/gestiona salas en memoria, reparte preguntas
 * por WebSocket y calcula puntajes. Es el reemplazo del código que antes
 * vivía embebido en TriviaController.
 */
@Service
public class GameRoomService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GameRoomService.class);

    private static final String CODIGO_SALA_DEFECTO = "PUBLICA-0";
    private static final int TIEMPO_LIMITE_SEGUNDOS = 15;
    private static final long TIEMPO_LIMITE_MS = TIEMPO_LIMITE_SEGUNDOS * 1000L;
    private static final int PUNTOS_BASE = 500;
    private static final int PUNTOS_BONUS_MAX = 500;
    private static final long DELAY_INICIO_MS = 2000L;
    private static final long DELAY_ENTRE_PREGUNTAS_MS = 4000L;

    private final Map<String, SalaJuego> salas = new ConcurrentHashMap<>();
    private final ExecutorService ejecutorPartidas = Executors.newCachedThreadPool(runnable -> {
        Thread hilo = new Thread(runnable, "sala-juego");
        hilo.setDaemon(true);
        return hilo;
    });

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PreguntaService preguntaService;

    @Autowired
    private RespuestaService respuestaService;

    @Autowired
    private PartidaService partidaService;

    @PreDestroy
    public void detener() {
        ejecutorPartidas.shutdownNow();
    }

    public void unirseJugador(String nombre, String codigo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            log.warn("Intento de unirse con nombre inválido a la sala {}", codigo);
            return;
        }

        String codigoSala = codigoOrDefault(codigo);
        SalaJuego sala = salas.computeIfAbsent(codigoSala, SalaJuego::new);
        sala.agregarJugador(nombre.trim());

        log.info("Jugador '{}' unido a la sala {}", nombre, codigoSala);
        messagingTemplate.convertAndSend("/topic/jugadoresActualizados/" + codigoSala, sala.ranking());
    }

    public void iniciarJuego(String codigo) {
        String codigoSala = codigoOrDefault(codigo);
        SalaJuego sala = salas.computeIfAbsent(codigoSala, SalaJuego::new);

        if (sala.isEnCurso()) {
            log.warn("Ya hay un juego en curso en la sala {}", codigoSala);
            return;
        }

        if (!sala.tieneJugadores()) {
            log.warn("No hay jugadores en la sala {}", codigoSala);
            messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigoSala,
                    EstadoJuegoDTO.error("No hay jugadores en la sala"));
            return;
        }

        try {
            List<Pregunta> preguntas = cargarPreguntasParaSala(codigoSala);
            if (preguntas.isEmpty()) {
                log.warn("No hay preguntas disponibles para la sala {}", codigoSala);
                messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigoSala,
                        EstadoJuegoDTO.error("No hay preguntas disponibles para esta sala"));
                return;
            }

            Collections.shuffle(preguntas);
            sala.setPreguntas(preguntas);
            sala.setEnCurso(true);

            messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigoSala,
                    EstadoJuegoDTO.iniciado(preguntas.size()));

            sala.setTareaJuego(ejecutorPartidas.submit(() -> ejecutarPartida(sala)));
        } catch (Exception e) {
            log.error("Error iniciando juego en sala {}: {}", codigoSala, e.getMessage(), e);
            sala.setEnCurso(false);
            messagingTemplate.convertAndSend("/topic/estadoJuego/" + codigoSala,
                    EstadoJuegoDTO.error(e.getMessage()));
        }
    }

    public List<Pregunta> cargarPreguntasParaSala(String codigo) {
        if (codigo.startsWith("PUBLICA-")) {
            Long idCategoria = null;
            try {
                idCategoria = Long.parseLong(codigo.split("-")[1]);
            } catch (Exception ignored) {
                // código público mal formado: se listan todas las preguntas
            }

            if (idCategoria != null) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(idCategoria);
                List<Pregunta> preguntasPublicas = preguntaService.findByCategoriaAndPartidaIsNull(categoria);
                if (!preguntasPublicas.isEmpty()) {
                    return preguntasPublicas;
                }
                return preguntaService.findByCategoria(categoria);
            }

            return preguntaService.findAll();
        }

        Partida partida = partidaService.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró una partida con código " + codigo));
        return preguntaService.findByPartida(partida);
    }

    private void ejecutarPartida(SalaJuego sala) {
        String codigo = sala.getCodigo();
        try {
            Thread.sleep(DELAY_INICIO_MS);

            List<Pregunta> preguntas = sala.getPreguntas();
            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta pregunta = preguntas.get(i);
                List<Respuesta> respuestas = respuestaService.findByPregunta(pregunta);
                if (respuestas.isEmpty()) {
                    continue;
                }

                Collections.shuffle(respuestas);
                sala.iniciarPregunta(pregunta.getIdPregunta());

                PreguntaDTO dto = new PreguntaDTO(
                        pregunta.getIdPregunta(),
                        pregunta.getTexto(),
                        respuestas.stream().map(r -> new RespuestaOpcionDTO(r.getIdRespuesta(), r.getTexto())).toList(),
                        i + 1,
                        preguntas.size(),
                        TIEMPO_LIMITE_SEGUNDOS);

                log.info("Enviando pregunta {} a sala {}", i + 1, codigo);
                messagingTemplate.convertAndSend("/topic/pregunta/" + codigo, dto);

                Thread.sleep(TIEMPO_LIMITE_MS);

                enviarResultado(sala, pregunta, respuestas);

                Thread.sleep(DELAY_ENTRE_PREGUNTAS_MS);
            }

            finalizarJuego(sala);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Partida de la sala {} interrumpida", codigo);
            sala.setEnCurso(false);
        } catch (Exception e) {
            log.error("Error durante la partida de la sala {}: {}", codigo, e.getMessage(), e);
            sala.setEnCurso(false);
        }
    }

    public void responder(String nombreJugador, String codigoSala, Long idRespuesta, long tiempoRespuestaMs) {
        String codigo = codigoOrDefault(codigoSala);
        SalaJuego sala = salas.get(codigo);
        if (sala == null || !sala.isEnCurso()) {
            log.warn("No hay una partida en curso en la sala {}", codigo);
            return;
        }

        Optional<Respuesta> opt = respuestaService.findById(idRespuesta);
        if (opt.isEmpty()) {
            log.warn("Respuesta no encontrada (ID: {})", idRespuesta);
            return;
        }

        Respuesta respuesta = opt.get();
        if (!sala.esPreguntaActual(respuesta.getPregunta().getIdPregunta())) {
            log.warn("'{}' respondió a una pregunta que ya no está vigente en la sala {}", nombreJugador, codigo);
            return;
        }

        if (!sala.marcarRespondio(nombreJugador)) {
            log.warn("'{}' ya había respondido esta pregunta en la sala {}", nombreJugador, codigo);
            return;
        }

        if (respuesta.isCorrecta()) {
            long tiempoTranscurrido = Math.max(0, Math.min(tiempoRespuestaMs, TIEMPO_LIMITE_MS));
            double factorVelocidad = (TIEMPO_LIMITE_MS - tiempoTranscurrido) / (double) TIEMPO_LIMITE_MS;
            int puntosGanados = PUNTOS_BASE + (int) (PUNTOS_BONUS_MAX * factorVelocidad);

            sala.sumarPuntos(nombreJugador, puntosGanados);
            log.info("'{}' respondió CORRECTO (+{} pts) en sala {}", nombreJugador, puntosGanados, codigo);
        } else {
            log.info("'{}' respondió INCORRECTO en sala {}", nombreJugador, codigo);
        }
    }

    private void enviarResultado(SalaJuego sala, Pregunta pregunta, List<Respuesta> respuestas) {
        Respuesta correcta = respuestas.stream()
                .filter(Respuesta::isCorrecta)
                .findFirst()
                .orElse(null);

        ResultadoDTO resultado = new ResultadoDTO(
                pregunta.getIdPregunta(),
                pregunta.getTexto(),
                correcta != null ? correcta.getIdRespuesta() : null,
                correcta != null ? correcta.getTexto() : null,
                sala.ranking());

        log.info("Enviando resultado de la pregunta {} en sala {}", pregunta.getIdPregunta(), sala.getCodigo());
        messagingTemplate.convertAndSend("/topic/resultado/" + sala.getCodigo(), resultado);
    }

    private void finalizarJuego(SalaJuego sala) {
        String codigo = sala.getCodigo();
        List<JugadorInfoDTO> rankingFinal = sala.ranking();

        log.info("Fin del juego en sala {} ({} jugadores)", codigo, rankingFinal.size());

        try {
            partidaService.limpiarAnfitrionPorCodigo(codigo);
        } catch (Exception e) {
            log.warn("No se pudo limpiar el anfitrión de la partida {}: {}", codigo, e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/finJuego/" + codigo, rankingFinal);

        // Sala se elimina por completo: la próxima partida con este código empieza limpia
        salas.remove(codigo);
    }

    private String codigoOrDefault(String codigo) {
        return (codigo == null || codigo.trim().isEmpty()) ? CODIGO_SALA_DEFECTO : codigo.trim();
    }
}
