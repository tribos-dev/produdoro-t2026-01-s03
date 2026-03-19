package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRestController;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

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

    // Teste usuario visualiza todas as suas tarefas

    // MockBean
    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void TestbuscarTodasTarefas () {
        String usuarioEmail = "teste@gmail.com";
        UUID idUsuario = UUID.randomUUID();

        Usuario usuarioMock = mock(Usuario.class);

        when(usuarioRepository.buscaUsuarioPorId(idUsuario))
                .thenReturn(usuarioMock);

        when(usuarioRepository.buscaUsuarioPorEmail(usuarioEmail))
                .thenReturn(usuarioMock);

        doNothing().when(usuarioMock).validaUsuario(idUsuario);

        Tarefa tarefaMock = mock(Tarefa.class);

        when(tarefaMock.getIdTarefa()).thenReturn(UUID.randomUUID());
        when(tarefaMock.getDescricao()).thenReturn("desc");

        List<Tarefa> tarefas = List.of(tarefaMock);

        when(tarefaRepository.buscaTarefaPorIdUsuario(idUsuario))
                .thenReturn(tarefas);

        List<TarefaListResponse> resultado =
                tarefaApplicationService.buscarTodasTarefas(usuarioEmail, idUsuario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(usuarioRepository).buscaUsuarioPorId(idUsuario);
        verify(usuarioRepository).buscaUsuarioPorEmail(usuarioEmail);
        verify(tarefaRepository).buscaTarefaPorIdUsuario(idUsuario);
    }

}
