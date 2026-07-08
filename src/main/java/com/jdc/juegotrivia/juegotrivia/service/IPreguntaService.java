package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Partida;

import java.util.List;
import java.util.Optional;

public interface IPreguntaService {

    List<Pregunta> findAll();

    Optional<Pregunta> findById(Long id);

    List<Pregunta> findByCategoria(Categoria categoria);

    List<Pregunta> findByPartida(Partida partida);

    List<Pregunta> findByCategoriaAndPartidaIsNull(Categoria categoria);

    Pregunta save(Pregunta pregunta);

    void delete(Long id);
}
