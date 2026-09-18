package br.unisales.gerenciador_projetos.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "TAREFA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TAREFA")
    private Long idTarefa;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDate dataInicio;

    private LocalDate prazo;

    private String prioridade;

    private String status;

    // Muitas tarefas pertencem a um projeto
    @ManyToOne
    @JoinColumn(name = "id_projeto", nullable = false)
    private Projeto projeto;

    // Muitas tarefas podem ter o mesmo responsável
    @ManyToOne
    @JoinColumn(name = "id_responsavel")
    private Usuario responsavel;

    // Uma tarefa pode possuir vários anexos
    @OneToMany(mappedBy = "tarefa")
    private List<Anexo> anexos;
}