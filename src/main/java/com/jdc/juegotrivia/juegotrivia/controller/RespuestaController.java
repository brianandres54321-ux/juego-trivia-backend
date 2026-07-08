package com.jdc.juegotrivia.juegotrivia.controller;

import com.jdc.juegotrivia.juegotrivia.model.Respuesta;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.service.RespuestaService;
import com.jdc.juegotrivia.juegotrivia.service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/respuestas")
@CrossOrigin(origins = "*")
public class RespuestaController {

    @Autowired
    private RespuestaService respuestaService;

    @Autowired
    private PreguntaService preguntaService;

    @GetMapping
    public List<Respuesta> listar() {
        return respuestaService.findAll();
    }

    @GetMapping("/pregunta/{idPregunta}")
    public List<Respuesta> listarPorPregunta(@PathVariable Long idPregunta) {
        Pregunta pregunta = preguntaService.findById(idPregunta).orElse(null);
        return (pregunta != null) ? respuestaService.findByPregunta(pregunta) : List.of();
    }

    @PostMapping
    public Respuesta crear(@RequestBody Respuesta respuesta) {
        return respuestaService.save(respuesta);
    }

    @PutMapping("/{id}")
    public Respuesta actualizar(@PathVariable Long id, @RequestBody Respuesta respuesta) {
        respuesta.setIdRespuesta(id);
        return respuestaService.save(respuesta);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        respuestaService.delete(id);
    }
}
