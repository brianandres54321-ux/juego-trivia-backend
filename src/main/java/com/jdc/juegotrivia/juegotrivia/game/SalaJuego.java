package com.jdc.juegotrivia.juegotrivia.game;

import com.jdc.juegotrivia.juegotrivia.dto.JugadorInfoDTO;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Estado en memoria de una sala de juego activa. Agrupa lo que antes eran
 * tres mapas paralelos (jugadores/puntos, preguntas y estado) que había que
 * mantener sincronizados a mano.
 */
public class SalaJuego {

    private final String codigo;
    private final Map<String, Integer> puntuaciones = new ConcurrentHashMap<>();
    private final Set<String> jugadoresQueRespondieron = ConcurrentHashMap.newKeySet();

    private volatile boolean enCurso = false;
    private volatile List<Pregunta> preguntas = List.of();
    private volatile Long idPreguntaActual;
    private volatile Future<?> tareaJuego;

    public SalaJuego(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    /** @return true si el jugador no existía en la sala y fue agregado. */
    public boolean agregarJugador(String nombre) {
        return puntuaciones.putIfAbsent(nombre, 0) == null;
    }

    public boolean tieneJugadores() {
        return !puntuaciones.isEmpty();
    }

    public void sumarPuntos(String nombre, int puntos) {
        puntuaciones.merge(nombre, puntos, Integer::sum);
    }

    public List<JugadorInfoDTO> ranking() {
        return puntuaciones.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> new JugadorInfoDTO(e.getKey(), e.getValue()))
                .toList();
    }

    public boolean isEnCurso() {
        return enCurso;
    }

    public void setEnCurso(boolean enCurso) {
        this.enCurso = enCurso;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public Future<?> getTareaJuego() {
        return tareaJuego;
    }

    public void setTareaJuego(Future<?> tareaJuego) {
        this.tareaJuego = tareaJuego;
    }

    /** Marca el inicio de una nueva pregunta y reinicia el registro de quién ya respondió. */
    public void iniciarPregunta(Long idPregunta) {
        this.idPreguntaActual = idPregunta;
        jugadoresQueRespondieron.clear();
    }

    public boolean esPreguntaActual(Long idPregunta) {
        return idPreguntaActual != null && idPreguntaActual.equals(idPregunta);
    }

    /** @return true si es la primera vez que este jugador responde la pregunta vigente. */
    public boolean marcarRespondio(String nombre) {
        return jugadoresQueRespondieron.add(nombre);
    }
}
