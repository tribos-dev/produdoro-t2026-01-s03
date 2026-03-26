package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveMudarParaPausaLonga(){
        Usuario usuario = DataHelper.createUsuarioFoco();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(usuarioRepository.salva(usuario)).thenReturn(usuario);

        usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail());
    }

    @Test
    void deveLancarExcecao_quandoUsuarioForDiferenteDoAutenticado() {
        Usuario usuario = DataHelper.createUsuarioFoco();
        UUID outroId = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        assertThrows(APIException.class, () ->
                usuarioApplicationService.iniciarPausaLonga(outroId, usuario.getEmail())
        );

        verify(usuarioRepository, never()).salva(any());
    }

    @Test
    void deveLancarExcecao_quandoUsuarioNaoEncontradoPorEmail() {
        Usuario usuario = DataHelper.createUsuarioFoco();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(null);

        assertThrows(Exception.class, () ->
                usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail())
        );
    }

    @Test
    void deveLancarExcecao_quandoUsuarioNaoEncontradoPorId() {
        Usuario usuario = DataHelper.createUsuarioFoco();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).
                thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        assertThrows(Exception.class, () ->
                usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail())
        );
    }

    @Test
    void deveSalvarUsuarioComStatusPausaLonga() {
        Usuario usuario = DataHelper.createUsuarioFoco();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(usuarioRepository.salva(any())).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail());

        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        verify(usuarioRepository, times(1)).salva(usuario);
    }
}