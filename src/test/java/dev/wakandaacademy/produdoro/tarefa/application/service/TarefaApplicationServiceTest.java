package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

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

    // Mock Bean
    @Mock
    UsuarioRepository usuarioRepository;

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

        // ASSERT (verify)
        verify(usuarioRepository).buscaUsuarioPorEmail(usuarioPorEmail);
        verify(usuarioRepository).buscaUsuarioPorId(usuario.getIdUsuario());
        //verify(usuario).validaUsuario(usuario.getIdUsuario());
        verify(tarefaRepository).buscaTarefaPorIdUsuario(usuario.getIdUsuario());
        verify(tarefaRepository).deletaTodasTarefas(tarefas);
    }

    @Test
    void deveRetornarTarefaConcluida() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();
        Tarefa tarefa = Tarefa.builder()
                .idTarefa(idTarefa)
                .status(StatusTarefa.A_FAZER)
                .idUsuario(usuario.getIdUsuario())
                .build();


        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.salva(tarefa)).thenReturn(tarefa);

        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusTarefa.CONCLUIDA, tarefa.getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoTarefaNaoExiste() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();


        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            tarefaApplicationService.concluiTarefa(usuario.getEmail(), idTarefa);
        });

    }

    @Test
    void deveLancarExcecaoQuandoTarefaJaEstiverConcluida() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();
        Tarefa tarefa = Tarefa.builder()
                .idTarefa(idTarefa)
                .status(StatusTarefa.CONCLUIDA)
                .idUsuario(usuario.getIdUsuario())
                .build();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        assertThrows(APIException.class, () -> {
            tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());
        });

        verify(tarefaRepository, never()).salva(any());

    }

}
