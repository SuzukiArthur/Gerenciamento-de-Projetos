package br.unisales.gerenciador_projetos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "PROJETOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROJETO")
    private Long idProjeto;

    @Column(nullable = false)
    private String nome;

    private String supervisor;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    private String status;

    @OneToMany(mappedBy = "projeto")
    private List<Tarefa> tarefas;

    @OneToMany(mappedBy = "projeto")
    private List<Equipe> equipes;
}