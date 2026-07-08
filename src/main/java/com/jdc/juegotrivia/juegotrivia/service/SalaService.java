package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Jugador;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SalaService {

    private final List<Jugador> jugadores = new ArrayList<>();

    public void agregarJugador(Jugador jugador) {
        jugadores.add(jugador);
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void iniciarJuego(String codigoSala) {
        System.out.println("Iniciando partida con código: " + codigoSala);
    }
}
