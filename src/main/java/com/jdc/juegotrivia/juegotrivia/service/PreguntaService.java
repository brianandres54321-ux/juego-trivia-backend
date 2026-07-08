package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.repository.PreguntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PreguntaService implements IPreguntaService {

    @Autowired
    private PreguntaRepository repository;

    @Override
    public List<Pregunta> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Pregunta> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Pregunta> findByCategoria(Categoria categoria) {
        return repository.findByCategoria(categoria);
    }

    // 🔹 Nuevo: preguntas de una partida privada específica
    @Override
    public List<Pregunta> findByPartida(Partida partida) {
        return repository.findByPartida(partida);
    }

    // 🔹 Nuevo: preguntas públicas (sin partida asignada)
    @Override
    public List<Pregunta> findByCategoriaAndPartidaIsNull(Categoria categoria) {
        return repository.findByCategoriaAndPartidaIsNull(categoria);
    }

    @Override
    public Pregunta save(Pregunta pregunta) {
        return repository.save(pregunta);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
