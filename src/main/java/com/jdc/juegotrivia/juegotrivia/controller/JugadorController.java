package com.jdc.juegotrivia.juegotrivia.controller;

import com.jdc.juegotrivia.juegotrivia.model.Jugador;
import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.service.JugadorService;
import com.jdc.juegotrivia.juegotrivia.service.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
@CrossOrigin(origins = { "https://*.vercel.app", "http://localhost:3000", "http://localhost:5173",
        "http://127.0.0.1:3000", "http://127.0.0.1:5173" }, allowCredentials = "true")
public class JugadorController {

    @Autowired
    private JugadorService jugadorService;

    @Autowired
    private PartidaService partidaService;

    @GetMapping
    public List<Jugador> listar() {
        return jugadorService.findAll();
    }

    @GetMapping("/partida/{codigoPartida}")
    public List<Jugador> listarPorPartida(@PathVariable String codigoPartida) {
        Partida partida = partidaService.findByCodigo(codigoPartida).orElse(null);
        return (partida != null) ? jugadorService.findByPartida(partida) : List.of();
    }

    @PostMapping("/registrar/{codigoPartida}")
    public Jugador registrar(@PathVariable String codigoPartida, @RequestBody Jugador jugador) {
        Partida partida = partidaService.findByCodigo(codigoPartida).orElseThrow();
        jugador.setPartida(partida);
        return jugadorService.save(jugador);
    }

    @PutMapping("/{id}/puntos/{puntos}")
    public Jugador actualizarPuntos(@PathVariable Long id, @PathVariable int puntos) {
        return jugadorService.actualizarPuntos(id, puntos);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        jugadorService.delete(id);
    }
}
