package br.unisales.gerenciador_projetos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String senha;

    private String funcao;

    @OneToMany(mappedBy = "responsavel")
    private List<Tarefa> tarefas;

    @OneToMany(mappedBy = "usuario")
    private List<Anexo> anexos;

    @OneToMany(mappedBy = "usuario")
    private List<Equipe> equipes;
}