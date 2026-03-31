package dev.wakandaacademy.produdoro.usuario.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.Email;
import java.util.UUID;

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

    public void pertenceAoUsuario(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario))
            throw APIException.build(HttpStatus.UNAUTHORIZED, "usuário(a) não autorizado(a) para a requisição solicitada!");
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

    public void iniciarPausaLonga(UUID idUsuario) {
        verificaUsuario(idUsuario);
        verificaSeEstaEmPausaLonga();
        this.status = StatusUsuario.PAUSA_LONGA;
    }

    private void verificaSeEstaEmPausaLonga() {
        if (this.status == StatusUsuario.PAUSA_LONGA) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuario já está em PAUSA LONGA");
        }
    }

    private void verificaUsuario(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de Autenticação não é Válida");
        }
    }

    private void statusEstaFoco() {
        if (!this.status.equals(StatusUsuario.FOCO)) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Usuario precisa estar em foco para incrementar pomodoro a uma tarefa!");
        }
    }

    public void validaUsuario(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é valida");
        }
    }

    public void mudaStatusparaFoco(UUID idUsuario) {
        pertenceAoUsuario(idUsuario);
        verificaSeEstaEmFoco();
        this.status = StatusUsuario.FOCO;
    }

    private void verificaSeEstaEmFoco() {
        if (this.status.equals(StatusUsuario.FOCO)) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já está em foco");
        }
    }

    public void mudaStatusParaPausaCurta(UUID idUsuario) {
        pertenceAoUsuario(idUsuario);
        verificaSeJaEstaEmPausaCurta();
        mudaStatusParaPausaCurta();
    }

    private void verificaSeJaEstaEmPausaCurta() {
        if (this.status.equals(StatusUsuario.PAUSA_CURTA)) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já está em pausa curta");
        }

    }

    private void mudaStatusParaPausaCurta() {
        this.status = StatusUsuario.PAUSA_CURTA;
    }
}
