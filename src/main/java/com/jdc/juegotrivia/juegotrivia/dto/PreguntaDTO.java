package com.jdc.juegotrivia.juegotrivia.dto;

import java.util.List;

public record PreguntaDTO(
        Long id,
        String texto,
        List<RespuestaOpcionDTO> respuestas,
        int numeroPregunta,
        int totalPreguntas,
        int tiempoLimite) {
}
