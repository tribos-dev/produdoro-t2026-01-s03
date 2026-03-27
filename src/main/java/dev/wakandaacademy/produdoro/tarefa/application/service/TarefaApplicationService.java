package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        int ordemTarefa = tarefaRepository.contaTarefasDoUsuario(tarefaRequest.getIdUsuario());
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest, ordemTarefa));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa =
                tarefaRepository.buscaTarefaPorId(idTarefa)
                        .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void deletaTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTodasTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefaPorIdUsuario(idUsuario);
        if (tarefas.isEmpty()) {
            throw APIException.build(HttpStatus.CONFLICT, "Usuário não possui tarefa(as) cadastrada(as)");
        }
        tarefaRepository.deletaTodasTarefas(tarefas);
        log.info("[finaliza] TarefaApplicationService - deletaTodasTarefas");
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
    public List<TarefaListResponse> buscarTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - buscarTodasTarefas");
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefa = tarefaRepository.buscaTarefaPorIdUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - buscarTodasTarefas");
        return TarefaListResponse.converte(tarefa);
    }

    @Override
    public void alteraOrdemTarefa(String email, UUID idTarefa, int novaPosicao) {
        log.info("[inicia] TarefaApplicationService - alteraOrdemTarefa");
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(email);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefaPorIdUsuario(usuario.getIdUsuario());
        Tarefa tarefa = tarefas.stream().filter(t -> t.getIdTarefa().equals(idTarefa))
                .findFirst()
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.alteraOrdem(tarefas, novaPosicao);
        tarefaRepository.salvaTarefas(tarefas);
        log.info("[finaliza] TarefaApplicationService - alteraOrdemTarefa");
    }
}
