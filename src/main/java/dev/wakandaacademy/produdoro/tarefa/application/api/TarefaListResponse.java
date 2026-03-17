package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

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
}
