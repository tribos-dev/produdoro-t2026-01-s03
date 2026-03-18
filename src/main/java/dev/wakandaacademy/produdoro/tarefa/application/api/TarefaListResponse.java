package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
public class TarefaListResponse {
    private UUID idTarefa;
    private String descricao;
    private UUID idUsuario;
    private UUID idArea;
    private UUID idProjeto;
    private StatusTarefa status;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;
    private int posicaoTarefa;

    public TarefaListResponse(Tarefa tarefa) {
        this.idTarefa = getIdTarefa();
        this.descricao = getDescricao();
        this.idUsuario = getIdUsuario();
        this.idArea = getIdArea();
        this.idProjeto = getIdProjeto();
        this.status = getStatus();
        this.statusAtivacao = getStatusAtivacao();
        this.contagemPomodoro = getContagemPomodoro();
        this.posicaoTarefa = getPosicaoTarefa();
    }

    public static List<TarefaListResponse> converte(List<Tarefa> tarefa) {
        return tarefa.stream().map(TarefaListResponse::new)
                .collect(Collectors.toList());
    }
}
