package com.jdc.juegotrivia.juegotrivia.dto;

import java.util.List;

public record ResultadoDTO(
        Long preguntaId,
        String preguntaTexto,
        Long respuestaCorrectaId,
        String respuestaCorrectaTexto,
        List<JugadorInfoDTO> ranking) {
}
