package com.jdc.juegotrivia.juegotrivia.repository;

import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
