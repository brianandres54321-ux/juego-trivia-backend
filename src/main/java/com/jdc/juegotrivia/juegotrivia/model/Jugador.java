package com.jdc.juegotrivia.juegotrivia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jugador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jugador")
    private Long idJugador;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "puntos")
    private Integer puntos = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partida")
    private Partida partida;
}
