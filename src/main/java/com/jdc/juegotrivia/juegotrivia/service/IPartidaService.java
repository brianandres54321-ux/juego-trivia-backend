package com.jdc.juegotrivia.juegotrivia.service;

import java.util.List;
import java.util.Optional;

import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Partida;

public interface IPartidaService {
    List<Partida> findAll();

    Optional<Partida> findById(Long id);

    Optional<Partida> findByCodigo(String codigo);

    Partida crearPartida(Categoria categoria);

    Partida crearPartidaPrivada(Partida partida);

    void limpiarAnfitrionPorCodigo(String codigo);

    void delete(Long id);
}
