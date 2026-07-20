package com.jdc.juegotrivia.juegotrivia.game;

import com.jdc.juegotrivia.juegotrivia.dto.JugadorInfoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaJuegoTest {

    @Test
    void noDeberiaDuplicarUnJugadorQueYaEstabaEnLaSala() {
        SalaJuego sala = new SalaJuego("ABC123");

        assertTrue(sala.agregarJugador("Ana"));
        assertFalse(sala.agregarJugador("Ana"));
    }

    @Test
    void soloDeberiaPermitirResponderUnaVezPorPregunta() {
        SalaJuego sala = new SalaJuego("ABC123");
        sala.iniciarPregunta(1L);

        assertTrue(sala.marcarRespondio("Ana"));
        assertFalse(sala.marcarRespondio("Ana"));

        sala.iniciarPregunta(2L);
        assertTrue(sala.marcarRespondio("Ana"));
    }

    @Test
    void deberiaRechazarRespuestasDePreguntasQueYaNoEstanVigentes() {
        SalaJuego sala = new SalaJuego("ABC123");
        sala.iniciarPregunta(1L);

        assertTrue(sala.esPreguntaActual(1L));

        sala.iniciarPregunta(2L);
        assertFalse(sala.esPreguntaActual(1L));
        assertTrue(sala.esPreguntaActual(2L));
    }

    @Test
    void elRankingDeberiaOrdenarDeMayorAMenorPuntaje() {
        SalaJuego sala = new SalaJuego("ABC123");
        sala.agregarJugador("Ana");
        sala.agregarJugador("Beto");

        sala.sumarPuntos("Ana", 500);
        sala.sumarPuntos("Beto", 900);
        sala.sumarPuntos("Ana", 600); // dos respuestas correctas acumulan

        List<JugadorInfoDTO> ranking = sala.ranking();

        assertEquals(2, ranking.size());
        assertEquals("Ana", ranking.get(0).nombre());
        assertEquals(1100, ranking.get(0).puntos());
        assertEquals("Beto", ranking.get(1).nombre());
    }
}
