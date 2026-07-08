package com.jdc.juegotrivia.juegotrivia.controller;

import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TriviaWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PreguntaService preguntaService;

    // ✅ Enviar preguntas a todos los jugadores
    public void enviarPregunta(Pregunta pregunta) {
        messagingTemplate.convertAndSend("/topic/pregunta", pregunta);
    }


}
