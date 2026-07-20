package com.jdc.juegotrivia.juegotrivia.dto;

public record EstadoJuegoDTO(String estado, Integer totalPreguntas, String mensaje) {

    public static EstadoJuegoDTO iniciado(int totalPreguntas) {
        return new EstadoJuegoDTO("iniciado", totalPreguntas, null);
    }

    public static EstadoJuegoDTO error(String mensaje) {
        return new EstadoJuegoDTO("error", null, mensaje);
    }
}
