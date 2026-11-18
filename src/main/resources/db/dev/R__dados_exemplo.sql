-- Dados de exemplo para o perfil local (branch feature/base-acesso-api, linha 4 do plano).
-- Senha de todos os usuários: senha123

insert into usuario (nome, email, senha_hash, perfil) values
    ('Carla Nogueira', 'carla.nogueira@utfpr.edu.br', '$2b$10$DTtPP.PsmEyfVC1.k/Zj.et/B0jMRLD7xJgMnaTnZ9Ks8THDZmJP2', 'PROFESSOR'),
    ('Diego Santos', 'diego.santos@alunos.utfpr.edu.br', '$2b$10$DTtPP.PsmEyfVC1.k/Zj.et/B0jMRLD7xJgMnaTnZ9Ks8THDZmJP2', 'MONITOR'),
    ('Marina Souza', 'marina@alunos.utfpr.edu.br', '$2b$10$DTtPP.PsmEyfVC1.k/Zj.et/B0jMRLD7xJgMnaTnZ9Ks8THDZmJP2', 'ESTUDANTE'),
    ('Lucas Prado', 'lucas@alunos.utfpr.edu.br', '$2b$10$DTtPP.PsmEyfVC1.k/Zj.et/B0jMRLD7xJgMnaTnZ9Ks8THDZmJP2', 'ESTUDANTE'),
    ('Beatriz Lima', 'beatriz@alunos.utfpr.edu.br', '$2b$10$DTtPP.PsmEyfVC1.k/Zj.et/B0jMRLD7xJgMnaTnZ9Ks8THDZmJP2', 'ESTUDANTE');

insert into turma (nome, codigo_acesso, professor_id, fim_semestre)
select 'Estruturas de Dados · ES41', 'K7M2QX', id, date '2026-12-18'
from usuario where email = 'carla.nogueira@utfpr.edu.br';

insert into matricula (turma_id, usuario_id)
select t.id, u.id
from turma t, usuario u
where t.codigo_acesso = 'K7M2QX'
  and u.email in ('marina@alunos.utfpr.edu.br', 'lucas@alunos.utfpr.edu.br', 'beatriz@alunos.utfpr.edu.br');

insert into aula (turma_id, numero, titulo, data, situacao)
select t.id, 12, 'Árvores AVL', date '2026-11-12', 'ABERTA'
from turma t where t.codigo_acesso = 'K7M2QX';
