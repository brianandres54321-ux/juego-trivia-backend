package com.jdc.juegotrivia.juegotrivia.controller;

import com.jdc.juegotrivia.juegotrivia.model.Jugador;
import com.jdc.juegotrivia.juegotrivia.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class JuegoController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SalaService salaService;

    // ✅ Registrar jugador nuevo
    @MessageMapping("/registrarJugador")
    public void registrarJugador(@Payload Jugador jugador) {
        salaService.agregarJugador(jugador);
        messagingTemplate.convertAndSend("/topic/jugadores", salaService.getJugadores());
    }


}
