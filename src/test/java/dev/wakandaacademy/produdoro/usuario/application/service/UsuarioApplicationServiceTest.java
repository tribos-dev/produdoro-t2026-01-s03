package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UsuarioApplicationServiceTest {
    @InjectMocks
    private UsuarioApplicationService usuarioApplicationService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void mudaStatusParaPausaCurta () {
        Usuario usuario = DataHelper.createUsuario() ;
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), usuario.getIdUsuario());
        assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
        verify(usuarioRepository).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository).buscaUsuarioPorId(usuario.getIdUsuario());
        verify(usuarioRepository).salva(usuario);


    }
}