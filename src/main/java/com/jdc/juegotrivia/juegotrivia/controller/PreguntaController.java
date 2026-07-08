package com.jdc.juegotrivia.juegotrivia.controller;

import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.service.PreguntaService;
import com.jdc.juegotrivia.juegotrivia.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preguntas")
@CrossOrigin(origins = "*")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<Pregunta> listar() {
        return preguntaService.findAll();
    }

    @GetMapping("/categoria/{idCategoria}")
    public List<Pregunta> listarPorCategoria(@PathVariable Long idCategoria) {
        Categoria categoria = categoriaService.findById(idCategoria).orElse(null);
        return (categoria != null) ? preguntaService.findByCategoria(categoria) : List.of();
    }

    @PostMapping
    public Pregunta crear(@RequestBody Pregunta pregunta) {
        return preguntaService.save(pregunta);
    }

    @PutMapping("/{id}")
    public Pregunta actualizar(@PathVariable Long id, @RequestBody Pregunta pregunta) {
        pregunta.setIdPregunta(id);
        return preguntaService.save(pregunta);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        preguntaService.delete(id);
    }
}
