package com.jdc.juegotrivia.juegotrivia.service;

import com.jdc.juegotrivia.juegotrivia.model.Partida;
import com.jdc.juegotrivia.juegotrivia.repository.CategoriaRepository;
import com.jdc.juegotrivia.juegotrivia.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    @Mock
    private PartidaRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private PartidaService service;

    @Test
    void deberiaLimpiarUsuarioAnfitrionCuandoTerminaLaPartida() {
        Partida partida = new Partida();
        partida.setCodigo("ABC12345");
        partida.setUsuario("BRIAN");

        when(repository.findByCodigo("ABC12345")).thenReturn(Optional.of(partida));

        service.limpiarAnfitrionPorCodigo("ABC12345");

        assertNull(partida.getUsuario());
        verify(repository).save(partida);
    }
}
