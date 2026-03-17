package dev.wakandaacademy.produdoro.usuario.application.api;

import javax.validation.Valid;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/public/v1/usuario")
public interface UsuarioAPI {
	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	UsuarioCriadoResponse postNovoUsuario(@RequestBody @Valid UsuarioNovoRequest usuarioNovo);

	@GetMapping(value = "/{idUsuario}")
	@ResponseStatus(code = HttpStatus.OK)
	UsuarioCriadoResponse buscaUsuarioPorId(@PathVariable UUID idUsuario);

	@GetMapping(value = "/landingpage")
	@ResponseStatus(code = HttpStatus.OK)
	List<TarefaListResponse> usuarioVerTarefa();
}
