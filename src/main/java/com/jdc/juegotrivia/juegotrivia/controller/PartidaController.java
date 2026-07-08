package com.jdc.juegotrivia.juegotrivia.controller;

import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.service.PartidaService;
import com.jdc.juegotrivia.juegotrivia.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/partidas")
@CrossOrigin(origins = "*")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<Partida> listar() {
        return partidaService.findAll();
    }

    @GetMapping("/{codigo}")
    public Optional<Partida> obtenerPorCodigo(@PathVariable String codigo) {
        return partidaService.findByCodigo(codigo);
    }

    @PostMapping("/crear/{idCategoria}")
    public Partida crear(@PathVariable Long idCategoria) {
        Categoria categoria = categoriaService.findById(idCategoria).orElseThrow();
        return partidaService.crearPartida(categoria);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        partidaService.delete(id);
    }

    @PostMapping("/crearPrivada")
    public Partida crearPrivada(@RequestBody Partida partida) {
        return partidaService.crearPartidaPrivada(partida);
    }


}
