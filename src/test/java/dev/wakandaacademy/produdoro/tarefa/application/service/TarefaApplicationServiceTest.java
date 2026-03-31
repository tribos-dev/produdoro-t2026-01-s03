package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;
    private String usuarioPorEmail = "email@email.com";

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);

        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request,1));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertNotNull(response.getIdTarefa());
    }

    @Test
    void deveLancarExcecaoQuandoTarefaNaoExistir() {
        Usuario usuario = DataHelper.createUsuarioFoco();
        UUID idTarefa = randomUUID();

        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), randomUUID())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        verify(tarefaRepository, never()).salva(any());
    }

    @Test
    void deveDeletarTarefaUsuario() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario())).thenReturn(tarefas);

        tarefaApplicationService.deletaTodasTarefas(usuario.getEmail(), usuario.getIdUsuario());

        doNothing().when(tarefaRepository).deletaTodasTarefas(tarefas);

        tarefaApplicationService.deletaTodasTarefas(usuarioPorEmail, usuario.getIdUsuario());

        verify(usuarioRepository, times(2)).buscaUsuarioPorEmail(usuarioPorEmail);
        verify(usuarioRepository, times(2)).buscaUsuarioPorId(usuario.getIdUsuario());
        verify(tarefaRepository, times(2)).buscaTarefaPorIdUsuario(usuario.getIdUsuario());
        verify(tarefaRepository, times(2)).deletaTodasTarefas(tarefas);
    }

    @Test
    void deveLancarExcecaoQuandoListaDeTarefasVazia() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = List.of();

        when(usuarioRepository.buscaUsuarioPorEmail(usuarioPorEmail))
                .thenReturn(usuario);

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario())).thenReturn(List.of());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTodasTarefas(usuario.getEmail(), usuario.getIdUsuario())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
    }

    // Teste usuario visualiza todas as suas tarefas

    @Test
    void deveBuscarTodasTarefas() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = usuario.getIdUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario())).thenReturn(tarefas);

        List<TarefaListResponse> resultado =
                tarefaApplicationService.buscarTodasTarefas(usuario.getEmail(), idUsuario);

        assertNotNull(resultado);
        assertEquals(8, resultado.size());
    }

    @Test
    void deveBuscarTodasTarefasSeListaVazia () {
        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = usuario.getIdUsuario();
        String usuarioEmail = "usuario@email.com";
        List<Tarefa> tarefas = List.of();

        when(usuarioRepository.buscaUsuarioPorId(idUsuario))
                .thenReturn(usuario);

        when(usuarioRepository.buscaUsuarioPorEmail(any()))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorIdUsuario(idUsuario))
                .thenReturn(tarefas);

        List<TarefaListResponse> resultado =
                tarefaApplicationService.buscarTodasTarefas(usuarioEmail, idUsuario);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());

    }

    @Test
    void deveEditarTarefaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        UUID idTarefa = tarefa.getIdTarefa();
        String descricao = "Nova descrição";

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        tarefaApplicationService.editaTarefa(usuario.getEmail(), idTarefa, descricao);

        assertEquals("Nova descrição", tarefa.getDescricao());
        verify(tarefaRepository).salva(tarefa);
    }

    @Test
    void deveLancarExcecaoQuandoEditarTarefaNaoEncontrada() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();
        String descricao = "Nova descricao";

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.editaTarefa(usuario.getEmail(), idTarefa, descricao)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForDonoDaTarefa() {
        Usuario usuario2 = DataHelper.createUsuario2();

        Tarefa tarefa = DataHelper.createTarefa();
        UUID idTarefa = tarefa.getIdTarefa();
        String descricao = "Nova descricao";

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario2.getEmail())).thenReturn(usuario2);

        assertThrows(APIException.class, () ->
                tarefaApplicationService.editaTarefa(usuario2.getEmail(), idTarefa, descricao)
        );
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoForInvalida() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        UUID idTarefa = tarefa.getIdTarefa();
        String descricao = "";

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        assertThrows(APIException.class, () ->
                tarefaApplicationService.editaTarefa(usuario.getEmail(), idTarefa, descricao));

    }
}