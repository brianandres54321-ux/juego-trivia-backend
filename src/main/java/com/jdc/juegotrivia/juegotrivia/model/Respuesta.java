package com.jdc.juegotrivia.juegotrivia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "respuesta")
@Getter
@Setter
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRespuesta;

    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private boolean correcta;

    @ManyToOne
    @JoinColumn(name = "id_pregunta")
    private Pregunta pregunta;
}
