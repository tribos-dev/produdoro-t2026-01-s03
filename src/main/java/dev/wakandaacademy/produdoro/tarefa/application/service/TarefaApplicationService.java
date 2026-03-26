package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
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
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioPorEmail.pertenceAoUsuario(usuarioPorEmail.getIdUsuario());
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(idUsuario);
        List<Tarefa> tarefasConcluidas = BuscaTarefasConcluidas(tarefas);
        validaSeExisteTarefasConcluidas(tarefasConcluidas);
        tarefaRepository.deletaTarefasConcluidas(tarefasConcluidas);
        log.info("[finaliza] TarefaApplicationService - deletaTarefasConcluidas");
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
}
