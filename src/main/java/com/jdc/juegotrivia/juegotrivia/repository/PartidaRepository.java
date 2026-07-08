package com.jdc.juegotrivia.juegotrivia.repository;

import com.jdc.juegotrivia.juegotrivia.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    Optional<Partida> findByCodigo(String codigo);
}
