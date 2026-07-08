package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Jugador;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import java.util.List;
import java.util.Optional;

public interface IJugadorService {
    List<Jugador> findAll();
    Optional<Jugador> findById(Long id);
    List<Jugador> findByPartida(Partida partida);
    Jugador save(Jugador jugador);
    Jugador actualizarPuntos(Long idJugador, int puntos);
    void delete(Long id);
}
