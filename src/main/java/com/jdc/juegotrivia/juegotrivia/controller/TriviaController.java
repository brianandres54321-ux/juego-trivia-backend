package com.jdc.juegotrivia.juegotrivia.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jdc.juegotrivia.juegotrivia.service.GameRoomService;

/**
 * Punto de entrada WebSocket/STOMP del juego. Toda la lógica del motor
 * (salas, puntajes, temporización de preguntas) vive en GameRoomService.
 */
@Controller
public class TriviaController {

    private static final Logger log = LoggerFactory.getLogger(TriviaController.class);

    @Autowired
    private GameRoomService gameRoomService;

    @MessageMapping("/nuevoJugador")
    public void nuevoJugador(@Payload Map<String, String> datos) {
        gameRoomService.unirseJugador(datos.get("nombre"), datos.get("codigo"));
    }

    @MessageMapping("/iniciarJuego")
    public void iniciarJuego(@Payload Map<String, String> datos) {
        gameRoomService.iniciarJuego(datos.get("codigo"));
    }

    @MessageMapping("/responder")
    public void procesarRespuesta(@Payload Map<String, Object> datos) {
        try {
            String nombreJugador = (String) datos.get("jugador");
            String codigoSala = (String) datos.get("codigoSala");
            Long idRespuesta = Long.parseLong(datos.get("idRespuesta").toString());
            long tiempoRespuesta = Long.parseLong(datos.get("tiempo").toString());

            gameRoomService.responder(nombreJugador, codigoSala, idRespuesta, tiempoRespuesta);
        } catch (Exception e) {
            log.warn("Payload de respuesta inválido: {}", e.getMessage());
        }
    }
}
