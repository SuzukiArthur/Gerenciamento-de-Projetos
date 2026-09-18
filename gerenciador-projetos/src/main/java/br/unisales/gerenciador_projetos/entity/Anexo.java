package br.unisales.gerenciador_projetos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ANEXO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    private String urlArquivo;

    private String nomeArquivo;

    private String tipoArquivo;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}