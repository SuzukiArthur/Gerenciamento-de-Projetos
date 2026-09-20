-- ========================================
-- 1. CRIAÇÃO DO BANCO DE DADOS
-- ========================================
CREATE DATABASE IF NOT EXISTS gestao_projetos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestao_projetos;

-- ========================================
-- 2. TABELA USUARIO
-- ========================================
CREATE TABLE IF NOT EXISTS USUARIO (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(100) NOT NULL,
    login VARCHAR(50) UNIQUE NOT NULL,
    Senha VARCHAR(255) NOT NULL,
    Função VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabela de usuários do sistema';

-- ========================================
-- 3. TABELA PROJETOS
-- ========================================
CREATE TABLE IF NOT EXISTS PROJETOS (
    ID_PROJETO INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único do projeto',
    NOME VARCHAR(100) NOT NULL COMMENT 'Nome do projeto',
    SUPERVISOR VARCHAR(100) COMMENT 'Supervisor responsável',
    ID_EQUIPE INT COMMENT 'ID da equipe responsável',
    DESCRIÇÃO TEXT COMMENT 'Descrição detalhada do projeto',
    DATA_INICIO DATE COMMENT 'Data de início do projeto',
    DATA_FIM DATE COMMENT 'Data prevista de término',
    STATUS VARCHAR(50) COMMENT 'Status: Planejamento, Em Andamento, Concluído, Cancelado'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabela de projetos do sistema';

-- ========================================
-- 4. TABELA EQUIPE
-- ========================================
CREATE TABLE IF NOT EXISTS EQUIPE (
    id_equipe INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único da equipe',
    nome VARCHAR(100) NOT NULL COMMENT 'Nome da equipe',
    descricao TEXT COMMENT 'Descrição da equipe',
    id_USUARIO INT COMMENT 'ID do usuário líder da equipe',
    id_PROJETO INT COMMENT 'ID do projeto associado',
    FOREIGN KEY (id_USUARIO) REFERENCES USUARIO(Id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (id_PROJETO) REFERENCES PROJETOS(ID_PROJETO) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabela de equipes do sistema';

-- Adiciona a Chave Estrangeira ID_EQUIPE na tabela PROJETOS caso ainda não exista
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'gestao_projetos' 
      AND TABLE_NAME = 'PROJETOS' 
      AND CONSTRAINT_NAME = 'fk_projetos_equipe'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE PROJETOS ADD CONSTRAINT fk_projetos_equipe FOREIGN KEY (ID_EQUIPE) REFERENCES EQUIPE(id_equipe) ON DELETE SET NULL ON UPDATE CASCADE;', 
    'SELECT "FK ja existe"'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 5. TABELA TAREFA
-- ========================================
CREATE TABLE IF NOT EXISTS TAREFA (
    ID_TAREFA INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único da tarefa',
    titulo VARCHAR(150) NOT NULL COMMENT 'Título da tarefa',
    descricao TEXT COMMENT 'Descrição detalhada da tarefa',
    data_inicio DATE COMMENT 'Data de início da tarefa',
    prazo DATE COMMENT 'Data de conclusão prevista',
    prioridade VARCHAR(20) COMMENT 'Nível de prioridade: Baixa, Média, Alta, Crítica',
    status VARCHAR(50) COMMENT 'Status: A fazer, Em andamento, Concluída, Bloqueada',
    id_projeto INT COMMENT 'ID do projeto ao qual a tarefa pertence',
    id_responsavel INT COMMENT 'ID do usuário responsável pela tarefa',
    FOREIGN KEY (id_projeto) REFERENCES PROJETOS(ID_PROJETO) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_responsavel) REFERENCES USUARIO(Id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabela de tarefas dos projetos';

-- ========================================
-- 6. TABELA ANEXO
-- ========================================
CREATE TABLE IF NOT EXISTS ANEXO (
    Id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único do anexo',
    tarefa_id INT NOT NULL COMMENT 'ID da tarefa ao qual o arquivo está anexado',
    url_arquivo VARCHAR(255) NOT NULL COMMENT 'Caminho ou URL do arquivo',
    nome_arquivo VARCHAR(150) NOT NULL COMMENT 'Nome do arquivo',
    tipo_arquivo VARCHAR(50) COMMENT 'Tipo MIME do arquivo',
    FOREIGN KEY (tarefa_id) REFERENCES TAREFA(ID_TAREFA) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabela de anexos das tarefas';

-- ========================================
-- FIM DO SCRIPT
-- ========================================