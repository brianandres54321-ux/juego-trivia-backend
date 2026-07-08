package com.jdc.juegotrivia.juegotrivia;

import com.jdc.juegotrivia.juegotrivia.controller.TriviaController;
import com.jdc.juegotrivia.juegotrivia.model.Categoria;
import com.jdc.juegotrivia.juegotrivia.model.Pregunta;
import com.jdc.juegotrivia.juegotrivia.service.PartidaService;
import com.jdc.juegotrivia.juegotrivia.service.PreguntaService;
import com.jdc.juegotrivia.juegotrivia.service.RespuestaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriviaControllerTest {

    @Mock
    private PreguntaService preguntaService;

    @Mock
    private RespuestaService respuestaService;

    @Mock
    private PartidaService partidaService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TriviaController controller;

    @Test
    void deberiaRecuperarPreguntasPorCategoriaCuandoNoHayPreguntasPublicas() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1L);

        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(10L);
        pregunta.setTexto("¿Cuál es la capital de Colombia?");
        pregunta.setCategoria(categoria);

        when(preguntaService.findByCategoriaAndPartidaIsNull(categoria)).thenReturn(List.of());
        when(preguntaService.findByCategoria(categoria)).thenReturn(List.of(pregunta));

        List<Pregunta> preguntas = controller.cargarPreguntasParaSala("PUBLICA-1");

        assertEquals(1, preguntas.size());
        assertSame(pregunta, preguntas.get(0));
    }
}
