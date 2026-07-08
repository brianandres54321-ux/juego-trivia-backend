package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Respuesta;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.repository.RespuestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RespuestaService implements IRespuestaService {

    @Autowired
    private RespuestaRepository repository;

    @Override
    public List<Respuesta> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Respuesta> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Respuesta> findByPregunta(Pregunta pregunta) {
        return repository.findByPregunta(pregunta);
    }

    @Override
    public Respuesta save(Respuesta respuesta) {
        return repository.save(respuesta);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
