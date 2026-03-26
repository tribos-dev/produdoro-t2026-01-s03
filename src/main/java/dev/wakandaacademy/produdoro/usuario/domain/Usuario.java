package dev.wakandaacademy.produdoro.usuario.domain;

import java.util.UUID;

import javax.validation.constraints.Email;

import dev.wakandaacademy.produdoro.handler.APIException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@Document(collection = "Usuario")
public class Usuario {
	@Id
	private UUID idUsuario;
	@Email
	@Indexed(unique = true)
	private String email;
	private ConfiguracaoUsuario configuracao;
	@Builder.Default
	private StatusUsuario status = StatusUsuario.FOCO;
	@Builder.Default
	private Integer quantidadePomodorosPausaCurta = 0;

	public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
		this.idUsuario = UUID.randomUUID();
		this.email = usuarioNovo.getEmail();
		this.status = StatusUsuario.FOCO;
		this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
	}


	public void incrementaPomodoro() {
		statusEstaFoco();
		quantidadePomodorosPausaCurta++;
		int limite = 3;
		if (quantidadePomodorosPausaCurta > limite) {
			this.status = StatusUsuario.PAUSA_LONGA;
			this.quantidadePomodorosPausaCurta = 0;
		} else {
			this.status = StatusUsuario.PAUSA_CURTA;
		}
	}
	public void iniciarPausaLonga(UUID idUsuario){
		verificaUsuario(idUsuario);
		verificaSeEstaEmPausaLonga();
		this.status = StatusUsuario.PAUSA_LONGA;
	}

	private void verificaSeEstaEmPausaLonga() {
		if(this.status == StatusUsuario.PAUSA_LONGA){
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuario já está em PAUSA LONGA");
		}
	}

	private void verificaUsuario(UUID idUsuario){
		if(!this.idUsuario.equals(idUsuario)){
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de Autenticação não é Válida");
		}
	}

    public void validaUsuario(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é valida");
		}
    }
}

	private void statusEstaFoco() {
		if (!status.equals(StatusUsuario.FOCO))
			throw APIException.build(HttpStatus.BAD_REQUEST, "Usuario precisa estar em foco para incrementar pomodoro a uma tarefa!");
	}
}