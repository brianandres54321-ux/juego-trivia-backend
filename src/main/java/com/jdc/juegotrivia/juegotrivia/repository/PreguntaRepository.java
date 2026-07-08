package com.jdc.juegotrivia.juegotrivia.repository;

import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByCategoria(Categoria categoria);
    List<Pregunta> findByPartida(Partida partida);
    List<Pregunta> findByCategoriaAndPartidaIsNull(Categoria categoria);



}
