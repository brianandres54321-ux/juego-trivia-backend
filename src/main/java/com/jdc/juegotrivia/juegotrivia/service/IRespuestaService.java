package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Respuesta;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import java.util.List;
import java.util.Optional;

public interface IRespuestaService {
    List<Respuesta> findAll();
    Optional<Respuesta> findById(Long id);
    List<Respuesta> findByPregunta(Pregunta pregunta);
    Respuesta save(Respuesta respuesta);
    void delete(Long id);
}
