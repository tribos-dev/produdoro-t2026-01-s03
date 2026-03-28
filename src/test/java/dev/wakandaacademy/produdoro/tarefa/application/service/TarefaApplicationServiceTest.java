package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();

        when(tarefaRepository.contaTarefasDoUsuario(any()))
                .thenReturn(1);
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request, 1));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }


    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    // Teste usuario deleta todas as suas tarefas
    @Test
    void deveIncrementarPomodoroQuandoTarefaPertenceAoUsuario() {
        Tarefa tarefa = DataHelper.createTarefa();
        Usuario usuario = DataHelper.createUsuarioFoco();
        UUID idTarefa = tarefa.getIdTarefa();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idTarefa);

        assertEquals(2, tarefa.getContagemPomodoro());
        verify(tarefaRepository).salva(tarefa);
    }

    @Test
    void deveLancarExcecaoQuandoTarefaNaoExistir() {
        Usuario usuario = DataHelper.createUsuarioFoco();
        UUID idTarefa = randomUUID();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idTarefa);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Tarefa não encontrada!", exception.getMessage());
        verify(tarefaRepository, never()).salva(any());
    }

    @Test
    void deveDeletarTarefaUsuario () {
        String usuarioPorEmail = "test@email";
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuarioPorEmail))
                .thenReturn(usuario);

        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario()))
                .thenReturn(tarefas);

        doNothing().when(tarefaRepository).deletaTodasTarefas(tarefas);

        tarefaApplicationService.deletaTodasTarefas(usuarioPorEmail, usuario.getIdUsuario());

        verify(usuarioRepository).buscaUsuarioPorEmail(usuarioPorEmail);
        verify(usuarioRepository).buscaUsuarioPorId(usuario.getIdUsuario());
        verify(tarefaRepository).buscaTarefaPorIdUsuario(usuario.getIdUsuario());
        verify(tarefaRepository).deletaTodasTarefas(tarefas);
    }

    @Test
    void excecaoTarefasVazia () {
        String usuarioPorEmail = "test@email";
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = List.of();

        when(usuarioRepository.buscaUsuarioPorEmail(usuarioPorEmail))
                .thenReturn(usuario);

        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario()))
                .thenReturn(tarefas);

        APIException e = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTodasTarefas(usuarioPorEmail, usuario.getIdUsuario())
        );

        assertEquals("Usuário não possui tarefa(as) cadastrada(as)", e.getMessage());
        assertEquals(HttpStatus.CONFLICT, e.getStatusException());
        verify(tarefaRepository, never()).deletaTodasTarefas(any());
    }

    // Teste usuario visualiza todas as suas tarefas

    @Test
    void deveBuscarTodasTarefas () {
        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = usuario.getIdUsuario();
        String usuarioEmail = "usuario@email.com";
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorId(idUsuario))
                .thenReturn(usuario);

        when(usuarioRepository.buscaUsuarioPorEmail(any()))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorIdUsuario(idUsuario))
                .thenReturn(tarefas);

        List<TarefaListResponse> resultado =
                tarefaApplicationService.buscarTodasTarefas(usuarioEmail, idUsuario);

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
    void deveAlterarOrdemTarefaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();

        Tarefa tarefa1 = Tarefa.builder()
                .idTarefa(UUID.randomUUID())
                .descricao("t1")
                .idUsuario(usuario.getIdUsuario())
                .ordemTarefa(1)
                .build();

        Tarefa tarefa2 = Tarefa.builder()
                .idTarefa(UUID.randomUUID())
                .descricao("t2")
                .idUsuario(usuario.getIdUsuario())
                .ordemTarefa(2)
                .build();

        List<Tarefa> tarefas = new ArrayList<>();
        tarefas.add(tarefa1);
        tarefas.add(tarefa2);

        String email = usuario.getEmail();

        when(usuarioRepository.buscaUsuarioPorEmail(email))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorId(tarefa1.getIdTarefa()))
                .thenReturn(Optional.of(tarefa1));

        when(tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario()))
                .thenReturn(tarefas);

        tarefaApplicationService.alteraOrdemTarefa(email, tarefa1.getIdTarefa(), 2);

        verify(tarefaRepository).salvaTarefas(tarefas);
    }

    @Test
    void deveLancarExcecao401QuandoUsuarioNaoForDonoDaTarefa() {
        UUID idUsuario = UUID.randomUUID();
        UUID idOutroUsuario = UUID.randomUUID();

        Usuario usuario = Usuario.builder()
                .idUsuario(idUsuario)
                .email("user@email.com")
                .build();

        UUID idTarefa = UUID.randomUUID();

        Tarefa tarefa = Tarefa.builder()
                .idTarefa(idTarefa)
                .descricao("teste")
                .idUsuario(idOutroUsuario) // diferente
                .ordemTarefa(1)
                .build();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorId(idTarefa))
                .thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.alteraOrdemTarefa(usuario.getEmail(), idTarefa, 1)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());

        verify(tarefaRepository, never()).salvaTarefas(any());
    }

    @Test
    void deveLancarExcecao404QuandoTarefaNaoExistirAlteraOrdem() {
        Usuario usuario = DataHelper.createUsuario();

        String email = usuario.getEmail();
        UUID idTarefa = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(email))
                .thenReturn(usuario);

        when(tarefaRepository.buscaTarefaPorId(idTarefa))
                .thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.alteraOrdemTarefa(email, idTarefa, 2)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Id da tarefa inválido!", exception.getMessage());

        verify(tarefaRepository, never()).salvaTarefas(any());
    }
}
