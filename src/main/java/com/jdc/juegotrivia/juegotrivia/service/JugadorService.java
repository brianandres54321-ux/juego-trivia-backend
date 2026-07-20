package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Jugador;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JugadorService implements IJugadorService {

    @Autowired
    private JugadorRepository repository;

    @Override
    public List<Jugador> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Jugador> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Jugador> findByPartida(Partida partida) {
        return repository.findByPartida(partida);
    }

    @Override
    public Jugador save(Jugador jugador) {
        return repository.save(jugador);
    }

    @Override
    public Jugador actualizarPuntos(Long idJugador, int puntos) {
        Optional<Jugador> jugadorOpt = repository.findById(idJugador);
        if (jugadorOpt.isPresent()) {
            Jugador jugador = jugadorOpt.get();
            jugador.setPuntos(jugador.getPuntos() + puntos);
            return repository.save(jugador);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

}
