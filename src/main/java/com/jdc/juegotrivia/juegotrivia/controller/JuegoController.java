package com.jdc.juegotrivia.juegotrivia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.jdc.juegotrivia.juegotrivia.model.Jugador;
import com.jdc.juegotrivia.juegotrivia.service.SalaService;

@Controller
public class JuegoController {

    @Autowired
    private SalaService salaService;

    public void registrarJugador(Jugador jugador) {
        salaService.agregarJugador(jugador);
    }
}
