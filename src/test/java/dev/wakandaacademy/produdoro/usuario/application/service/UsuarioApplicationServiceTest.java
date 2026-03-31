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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UsuarioApplicationServiceTest {
    @InjectMocks
    private UsuarioApplicationService usuarioApplicationService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void mudaStatusParaPausaCurta() {
        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), usuario.getIdUsuario());
        assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
        verify(usuarioRepository).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository).salva(usuario);


    }

    @Test
    void naoDeveMudarStatusParaPausaCurtaQuandoUsuarioJaEstaEmPausaCurta() {
        Usuario usuario = DataHelper.createUsuarioPausaCurta();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                .thenReturn(usuario);

        assertThrows(APIException.class, () ->
                usuarioApplicationService.mudaStatusParaPausaCurta(
                        usuario.getEmail(),
                        usuario.getIdUsuario()
                )
        );
        assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());

        verify(usuarioRepository).buscaUsuarioPorEmail(usuario.getEmail());
    }

    @Test
    void deveMudarParaPausaLonga() {
        Usuario usuario = DataHelper.createUsuarioFoco();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(usuarioRepository.salva(usuario)).thenReturn(usuario);

        usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail());

        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        verify(usuarioRepository).salva(usuario);
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
    void deveLancarExcecao_quandoUsuarioNaoEncontradoPorId() {
        Usuario usuario = DataHelper.createUsuarioFoco();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        assertThrows(APIException.class, () ->
                usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail())
        );
    }
}
