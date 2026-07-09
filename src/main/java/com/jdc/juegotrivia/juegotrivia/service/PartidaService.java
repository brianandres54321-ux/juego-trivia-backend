package com.jdc.juegotrivia.juegotrivia.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.repository.CategoriaRepository;
import com.jdc.juegotrivia.juegotrivia.repository.PartidaRepository;

@Service
public class PartidaService implements IPartidaService {

    @Autowired
    private PartidaRepository repository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Partida> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Partida> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Partida> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    @Override
    public Partida crearPartida(Categoria categoria) {
        String codigo = generarCodigoUnico();
        Partida partida = new Partida();
        partida.setCodigo(codigo);
        partida.setCategoria(categoria);
        partida.setFechaCreacion(LocalDateTime.now());
        partida.setPuntos(0);
        partida.setUsuario(null);
        return repository.save(partida);
    }

    /**
     * 🆕 Crear una partida privada desde una solicitud que incluye categoría y
     * usuario.
     */
    public Partida crearPartidaPrivada(Partida partida) {
        String codigo = generarCodigoUnico();

        // ✅ Validar y cargar la categoría existente
        if (partida.getCategoria() != null && partida.getCategoria().getIdCategoria() != null) {
            Long idCategoria = partida.getCategoria().getIdCategoria();
            Categoria categoria = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + idCategoria));
            partida.setCategoria(categoria);
        } else {
            throw new RuntimeException("Debe especificar una categoría válida.");
        }

        // ✅ Asignar código único y valores por defecto
        partida.setCodigo(codigo);
        partida.setFechaCreacion(LocalDateTime.now());

        if (partida.getPuntos() == null) {
            partida.setPuntos(0);
        }

        if (partida.getUsuario() == null || partida.getUsuario().trim().isEmpty()) {
            partida.setUsuario(null);
        }

        return repository.save(partida);
    }

    @Override
    public void limpiarAnfitrionPorCodigo(String codigo) {
        repository.findByCodigo(codigo).ifPresent(partida -> {
            partida.setUsuario(null);
            repository.save(partida);
        });
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private String generarCodigoUnico() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
