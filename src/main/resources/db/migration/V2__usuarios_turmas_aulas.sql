create table usuario (
    id bigserial primary key,
    nome varchar(120) not null,
    email varchar(160) not null unique,
    senha_hash varchar(100) not null,
    perfil varchar(20) not null,
    criado_em timestamptz not null default now()
);

create table turma (
    id bigserial primary key,
    nome varchar(120) not null,
    codigo_acesso varchar(6) not null unique,
    professor_id bigint not null references usuario(id),
    fim_semestre date not null,
    dados_apagados_em timestamptz
);

create table matricula (
    turma_id bigint not null references turma(id),
    usuario_id bigint not null references usuario(id),
    criada_em timestamptz not null default now(),
    primary key (turma_id, usuario_id)
);

create table aula (
    id bigserial primary key,
    turma_id bigint not null references turma(id),
    numero integer not null,
    titulo varchar(160) not null,
    data date not null,
    situacao varchar(20) not null,
    prazo_pos_aula timestamptz,
    encerrada_em timestamptz,
    unique (turma_id, numero)
);
