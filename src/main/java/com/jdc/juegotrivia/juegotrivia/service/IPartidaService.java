package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import java.util.List;
import java.util.Optional;

public interface IPartidaService {
    List<Partida> findAll();
    Optional<Partida> findById(Long id);
    Optional<Partida> findByCodigo(String codigo);
    Partida crearPartida(Categoria categoria);
    Partida crearPartidaPrivada(Partida partida);
    void delete(Long id);
}
