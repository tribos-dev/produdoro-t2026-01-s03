package dev.wakandaacademy.produdoro.tarefa.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Document(collection = "Tarefa")
public class Tarefa {
	@Id
	private UUID idTarefa;
	@NotBlank
	private String descricao;
	@Indexed
	private UUID idUsuario;
	@Indexed
	private UUID idArea;
	@Indexed
	private UUID idProjeto;
	private StatusTarefa status;
	private StatusAtivacaoTarefa statusAtivacao;
	private int contagemPomodoro;
	private int ordemTarefa;

	public Tarefa(TarefaRequest tarefaRequest,int ordemTarefa) {
		this.idTarefa = UUID.randomUUID();
		this.idUsuario = tarefaRequest.getIdUsuario();
		this.descricao = tarefaRequest.getDescricao();
		this.idArea = tarefaRequest.getIdArea();
		this.idProjeto = tarefaRequest.getIdProjeto();
		this.status = StatusTarefa.A_FAZER;
		this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
		this.contagemPomodoro = 1;
		this.ordemTarefa = ordemTarefa + 1;
	}

	public void pertenceAoUsuario(Usuario usuarioPorEmail) {
		if(!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário não é dono da Tarefa solicitada!");
		}
	}

    public void mudaStatusParaConcluida() {
		VeficaSeTarefaFoiConcluida();
		this.status = StatusTarefa.CONCLUIDA;
	}

	private void VeficaSeTarefaFoiConcluida() {
		if (this.status == StatusTarefa.CONCLUIDA) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Essa Tarefa Já Está Concluida");
		}
	}

	public void incrementaPomodoro(Usuario usuario) {
		pertenceAoUsuario(usuario);
		usuario.incrementaPomodoro();
		this.contagemPomodoro++;
	}

    public void alteraOrdem(List<Tarefa> tarefas, int novaPosicao) {

        int posicaoAtual = this.getOrdemTarefa();

        verificaLimitesDePosicoes(tarefas, novaPosicao);
        verificaSePosicaoEIgual(novaPosicao, posicaoAtual);
        alteraPosicoesDeOutrasTarefas(tarefas, novaPosicao, posicaoAtual);
        this.ordemTarefa = novaPosicao;
    }

    private void alteraPosicoesDeOutrasTarefas(List<Tarefa> tarefas, int novaPosicao, int posicaoAtual) {
        if (posicaoAtual > novaPosicao) {
            for (Tarefa t : tarefas) {
                if (t.getOrdemTarefa() >= novaPosicao && t.getOrdemTarefa() < posicaoAtual) {
                    t.ordemTarefa++;
                }
            }
        }else {
            for (Tarefa t : tarefas) {
                if (t.getOrdemTarefa() > posicaoAtual && t.getOrdemTarefa() <= novaPosicao) {
                    t.ordemTarefa--;
                }
            }
        }
    }

    private void verificaSePosicaoEIgual(int novaPosicao, int posicaoAtual) {
        if (posicaoAtual == novaPosicao) {
            throw APIException.build(HttpStatus.CONFLICT, "Posição da Tarefa é igual a nova posicao");
        }
    }

    private void verificaLimitesDePosicoes(List<Tarefa> tarefas, int novaPosicao) {
        if(novaPosicao < 0 || novaPosicao > tarefas.size()) {
            throw APIException.build(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Posição não pode ser negativa ou maior que o numero de tarefas!");
        }
    }

	public void desativaTarefa() {
		this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
	}

	public void ativaTarefa() {
		if (this.statusAtivacao.equals(StatusAtivacaoTarefa.ATIVA)) {
			throw APIException.build(HttpStatus.CONFLICT, "Tarefa já está ativa!");
		}
		this.statusAtivacao = StatusAtivacaoTarefa.ATIVA;
	}
}
