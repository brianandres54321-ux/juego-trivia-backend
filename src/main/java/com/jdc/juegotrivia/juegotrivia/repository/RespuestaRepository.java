package com.jdc.juegotrivia.juegotrivia.repository;

import com.jdc.juegotrivia.juegotrivia.model.Respuesta;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {
    List<Respuesta> findByPregunta(Pregunta pregunta);
}
