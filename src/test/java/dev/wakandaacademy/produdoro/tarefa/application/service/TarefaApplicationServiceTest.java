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

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);

        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertNotNull(response.getIdTarefa());
    }

    @Test
    void deveLancarExcecaoQuandoTarefaNaoExistir() {
        Usuario usuario = DataHelper.createUsuarioFoco();

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

        verify(tarefaRepository).deletaTodasTarefas(tarefas);
    }

    @Test
    void deveLancarExcecaoQuandoListaDeTarefasVazia() {
        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario())).thenReturn(List.of());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTodasTarefas(usuario.getEmail(), usuario.getIdUsuario())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
    }

    @Test
    void deveBuscarTodasTarefas() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario())).thenReturn(tarefas);

        List<TarefaListResponse> resultado =
                tarefaApplicationService.buscarTodasTarefas(usuario.getEmail(), usuario.getIdUsuario());

        assertNotNull(resultado);
    }

    @Test
    void deveConcluirTarefa() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa()))
                .thenReturn(Optional.of(tarefa));

        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusTarefa.CONCLUIDA, tarefa.getStatus());
    }

    @Test
    void deveDeletarTarefasConcluidas() {
        Usuario usuario = DataHelper.createUsuario();

        List<Tarefa> tarefas = List.of(
                Tarefa.builder()
                        .idUsuario(usuario.getIdUsuario())
                        .status(StatusTarefa.CONCLUIDA)
                        .build()
        );

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidas(usuario.getIdUsuario())).thenReturn(tarefas);

        tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail(), usuario.getIdUsuario());

        verify(tarefaRepository).deletaTarefasConcluidas(anyList());
    }

    @Test
    void deveLancarExecaoQuandoNaoExitirTarefasConcluidasPraDeletar() {
        Usuario usuario = DataHelper.createUsuario();

        List<Tarefa> tarefas = List.of(
                Tarefa.builder()
                        .idUsuario(usuario.getIdUsuario())
                        .status(StatusTarefa.CONCLUIDA)
                        .build()
        );

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidas(usuario.getIdUsuario())).thenReturn(Collections.emptyList());

        assertThrows(APIException.class, () -> tarefaApplicationService.
                deletaTarefasConcluidas(usuario.getEmail(), usuario.getIdUsuario()));
    }

//    @Test
//    void deveLancarExcecaoQuandoTarefaJaConcluida() {
//        Usuario usuario = DataHelper.createUsuario();
//        Tarefa tarefa = DataHelper.createTarefa();
//        tarefa.setStatus(StatusTarefa.CONCLUIDA);
//
//        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
//        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa()))
//                .thenReturn(Optional.of(tarefa));
//
//        assertThrows(APIException.class, () ->
//                tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa())
//        );
//    }

    @Test
    void deveDefinirTarefaComoAtivaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();
        Tarefa tarefa = DataHelper.createTarefa();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaAtivaDoUsuario(usuario)).thenReturn(Optional.empty());

        tarefaApplicationService.defineTarefaComoAtiva(usuario.getEmail(), idTarefa);

        assertEquals(StatusAtivacaoTarefa.ATIVA, tarefa.getStatusAtivacao());
        verify(tarefaRepository).salva(tarefa);
    }

    @Test
    void deveLancarConflitoQuandoTarefaJaEstaAtiva() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefaAtiva();
        UUID idTarefa = tarefa.getIdTarefa();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaAtivaDoUsuario(usuario)).thenReturn(Optional.of(tarefa));

        APIException excecao = assertThrows(APIException.class,
                () -> tarefaApplicationService.defineTarefaComoAtiva(usuario.getEmail(), idTarefa));

        assertEquals(HttpStatus.CONFLICT, excecao.getStatusException());
        assertEquals("Tarefa já está ativa!", excecao.getMessage());
        verify(tarefaRepository, never()).salva(any());
    }
}