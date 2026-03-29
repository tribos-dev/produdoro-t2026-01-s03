package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;


    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa =
                tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public List<TarefaListResponse> buscarTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - buscarTodasTarefas");
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefaPorIdUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - buscarTodasTarefas");
        return TarefaListResponse.converte(tarefas);
    }

    @Override
    public void deletaTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTodasTarefas");
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefaPorIdUsuario(idUsuario);
        if (tarefas.isEmpty()) {
            throw APIException.build(HttpStatus.CONFLICT, "Usuário não possui tarefa(as) cadastrada(as)");
        }
        tarefaRepository.deletaTodasTarefas(tarefas);
        log.info("[finaliza] TarefaApplicationService - deletaTodasTarefas");
    }

    @Override
    public void concluiTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - concluiTarefa");
        Tarefa tarefa = detalhaTarefa(usuario, idTarefa);
        if (tarefa.getStatus() == StatusTarefa.CONCLUIDA) {
            throw APIException.build(HttpStatus.BAD_REQUEST,"Tarefa já está concuída");
        }
        tarefa.mudaStatusParaConcluida();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - concluiTarefa");
    }

    @Override
    public void deletaTarefasConcluidas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTarefasConcluidas");
        Usuario usuarioPorId = usuarioRepository.buscaUsuarioPorId(idUsuario);
        pertenceAoUsuario(usuario, idUsuario);
        List<Tarefa> tarefasConcluidas = tarefaRepository.buscaTarefasConcluidas(idUsuario);
        if (tarefasConcluidas == null || tarefasConcluidas.isEmpty()) {
            throw APIException.build(HttpStatus.CONFLICT,
                    "Usuário não possui tarefas concluídas para deletar"
            );
        }

        tarefaRepository.deletaTarefasConcluidas(tarefasConcluidas);

        log.info("[finaliza] TarefaApplicationService - deletaTarefasConcluidas");
    }

    private void pertenceAoUsuario(String usuarioEmail, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - validaUsuario");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuarioEmail);
        usuarioPorEmail.pertenceAoUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - validaUsuario");
    }


    private void validaSeExisteTarefasConcluidas(List<Tarefa> tarefasConcluidas) {
        if (tarefasConcluidas.isEmpty())
            throw APIException.build(HttpStatus.NOT_FOUND, "Usuário não possui nenhuma tarefa concluída!");
    }

    private List<Tarefa> BuscaTarefasConcluidas(List<Tarefa> tarefas) {
        return tarefas.stream()
                .filter(tarefa -> tarefa.getStatus() == StatusTarefa.CONCLUIDA)
                .collect(Collectors.toList());
    }
    public void incrementaPomodoro(String usuario, UUID idTarefa) {
        log.info("[start] TarefaApplicationService - incrementaPomodoro");
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        tarefa.incrementaPomodoro(usuarioPorEmail);
        tarefaRepository.salva(tarefa);
        usuarioRepository.salva(usuarioPorEmail);
        log.debug("[finish] TarefaApplicationService - incrementaPomodoro");
    }

    @Override
    public void defineTarefaComoAtiva(String usuarioEmail, UUID idTarefa) {
        log.info("[start] TarefaApplicationService - defineTarefaComoAtiva");
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuarioEmail);
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        tarefaRepository.buscaTarefaAtivaDoUsuario(usuarioPorEmail).ifPresent(
                tarefaAntiga -> {
                    tarefaAntiga.desativaTarefa();
                    tarefaRepository.salva(tarefaAntiga);
                }
        );
        tarefa.ativaTarefa();
        tarefaRepository.salva(tarefa);
        log.info("[finish] TarefaApplicationService - defineTarefaComoAtiva");
    }
}
