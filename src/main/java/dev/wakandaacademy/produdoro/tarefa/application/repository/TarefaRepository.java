package dev.wakandaacademy.produdoro.tarefa.application.repository;

import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository {

    Tarefa salva(Tarefa tarefa);
    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);
    List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario);
    void deletaTarefasConcluidas(List<Tarefa> tarefasConcluidas);
    void deletaTodasTarefas(List<Tarefa> tarefas);
    List<Tarefa> buscaTarefaPorIdUsuario(UUID idUsuario);
    void salvaTarefas(List<Tarefa> tarefas);
    int contaTarefasDoUsuario(UUID idUsuario);
    List<Tarefa> buscaTarefasConcluidas(UUID idUsuario);
    Optional<Tarefa> buscaTarefaAtivaDoUsuario(Usuario usuarioPorEmail);
}
